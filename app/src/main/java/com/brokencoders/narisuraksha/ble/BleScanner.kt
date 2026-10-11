package com.brokencoders.narisuraksha.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.core.DeviceIdProvider
import com.brokencoders.narisuraksha.core.PermissionHelper
import com.brokencoders.narisuraksha.core.SosPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Scans for nearby BLE SOS broadcasts.
 * Supports 16-bit Service UUID, 128-bit UUID, Manufacturer Data fallback, and raw packet scanning.
 * Includes both hardware-filtered scan and software-filtered debug scan mode for reliable cross-OEM discovery.
 */
class BleScanner(
    private val context: Context,
    private val deviceIdProvider: DeviceIdProvider,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter
    private var scanner: BluetoothLeScanner? = null

    private val _received = MutableSharedFlow<ReceivedSos>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val received: Flow<ReceivedSos> = _received.asSharedFlow()

    var isScanning = false
        private set

    private var fallbackScanAttempted = false

    /** RelayDedupCache with 60-second expiration */
    val dedupCache = RelayDedupCache()

    /** Budget tracker enforcing MESH_MAX_RELAYS_PER_MIN */
    private val relayBudgetTracker = RelayBudgetTracker()

    /** Callback invoked when a packet is scheduled for mesh relay forwarding */
    var relayHandler: ((SosPacket) -> Unit)? = null

    // Per-sender cooldown map
    private val senderLastSeenMap = ConcurrentHashMap<Short, Long>()
    // Sliding window of alert timestamps across all senders (global rate limit against spoofed ID rotations)
    private val globalAlertTimestamps = ConcurrentLinkedQueue<Long>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            processScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach { processScanResult(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isScanning = false
            BleDiagnosticsTracker.setScanning(false)
            val errorMsg = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "ALREADY_STARTED (Code 1)"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "REGISTRATION_FAILED (Code 2)"
                SCAN_FAILED_INTERNAL_ERROR -> "INTERNAL_ERROR (Code 3)"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "FEATURE_UNSUPPORTED (Code 4)"
                else -> "Error Code $errorCode"
            }
            BleDiagnosticsTracker.recordEvent("NARI_BLE_SCAN_FAILURE", "Scan failed: $errorMsg", isError = true)
            Log.e(TAG, "BLE Scan failed with error code: $errorMsg")

            // Auto-fallback: If hardware filtering fails, restart without filters
            if (!fallbackScanAttempted) {
                fallbackScanAttempted = true
                BleDiagnosticsTracker.recordEvent("NARI_BLE_SCAN_START", "Auto-recovering: restarting scanner without hardware filters...")
                startScanWithoutFilters()
            }
        }
    }

    private fun processScanResult(result: ScanResult?) {
        if (result == null) return
        val scanRecord = result.scanRecord ?: return

        // 1. Try 16-bit Service Data via Android API
        var payloadBytes = scanRecord.getServiceData(Constants.SOS_PARCEL_UUID)

        // 2. Try 128-bit Legacy Service Data
        if (payloadBytes == null) {
            payloadBytes = scanRecord.getServiceData(ParcelUuid(Constants.SOS_LEGACY_128_UUID))
        }

        // 3. Try Manufacturer Data fallback
        if (payloadBytes == null) {
            payloadBytes = scanRecord.getManufacturerSpecificData(Constants.MANUFACTURER_ID)
        }

        // 4. Scan all available service data entries if UUID variation occurred
        if (payloadBytes == null && scanRecord.serviceData != null) {
            for ((uuid, data) in scanRecord.serviceData) {
                if (uuid.uuid.toString().contains("fde1", ignoreCase = true) ||
                    data.size >= PacketCodec.LEGACY_PACKET_SIZE_BYTES
                ) {
                    payloadBytes = data
                    break
                }
            }
        }

        // 5. Deep Raw-Byte Fallback: Search raw advertisement frame for 0xFDE1 or packet magic
        if (payloadBytes == null && scanRecord.bytes != null) {
            payloadBytes = extractPayloadFromRawBytes(scanRecord.bytes)
        }

        if (payloadBytes == null) return

        val packet = PacketCodec.decode(payloadBytes)
        if (packet == null) {
            BleDiagnosticsTracker.incrementRejectedPackets()
            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_PACKET_REJECTED",
                "Failed to decode packet of length ${payloadBytes.size} bytes from ${result.device.address}"
            )
            return
        }

        val now = System.currentTimeMillis()

        val decision = RelayPolicy.evaluate(
            packet = packet,
            myDeviceId = deviceIdProvider.deviceId,
            cache = dedupCache,
            canRelayBudget = { relayBudgetTracker.tryAcquire() }
        )

        if (decision != RelayDecision.IGNORE_OWN) {
            BleDiagnosticsTracker.setLastReceivedTtl(packet.ttl)
        }

        when (decision) {
            RelayDecision.IGNORE_OWN -> return

            RelayDecision.DROP_DUPLICATE -> {
                BleDiagnosticsTracker.incrementDuplicatesDropped()
                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_DUPLICATE_DROPPED",
                    "DROP dup sender=${packet.senderId}"
                )
                return
            }

            RelayDecision.RELAY_ONLY -> {
                scheduleRelay(packet)
                return
            }

            RelayDecision.ALERT_AND_RELAY -> {
                scheduleRelay(packet)
                processLocalAlert(packet, result.rssi, now)
            }

            RelayDecision.ALERT_ONLY -> {
                processLocalAlert(packet, result.rssi, now)
            }

            RelayDecision.CONSUME_ACK_FOR_ME -> {
                BleDiagnosticsTracker.incrementAcksReceived()
                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_ACK_RECEIVED",
                    "RX sender=${packet.senderId} ttl=${packet.ttl} rssi=${result.rssi}"
                )
                val receivedSos = ReceivedSos(packet = packet, rssi = result.rssi, receivedAt = now)
                _received.tryEmit(receivedSos)
            }
        }
    }

    /**
     * Injects a simulated packet directly into the scanner pipeline.
     * Useful for emulator testing where Bluetooth hardware is unavailable.
     */
    fun injectSimulatedPacket(packet: SosPacket, rssi: Int = -65) {
        val now = System.currentTimeMillis()
        val decision = RelayPolicy.evaluate(
            packet = packet,
            myDeviceId = deviceIdProvider.deviceId,
            cache = dedupCache,
            canRelayBudget = { relayBudgetTracker.tryAcquire() }
        )

        if (decision != RelayDecision.IGNORE_OWN) {
            BleDiagnosticsTracker.setLastReceivedTtl(packet.ttl)
        }

        when (decision) {
            RelayDecision.IGNORE_OWN -> return
            RelayDecision.DROP_DUPLICATE -> {
                BleDiagnosticsTracker.incrementDuplicatesDropped()
                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_DUPLICATE_DROPPED",
                    "DROP dup sender=${packet.senderId}"
                )
            }
            RelayDecision.RELAY_ONLY -> scheduleRelay(packet)
            RelayDecision.ALERT_AND_RELAY -> {
                scheduleRelay(packet)
                processLocalAlert(packet, rssi, now)
            }
            RelayDecision.ALERT_ONLY -> processLocalAlert(packet, rssi, now)
            RelayDecision.CONSUME_ACK_FOR_ME -> {
                BleDiagnosticsTracker.incrementAcksReceived()
                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_ACK_RECEIVED",
                    "RX sender=${packet.senderId} ttl=${packet.ttl} rssi=$rssi"
                )
                val receivedSos = ReceivedSos(packet = packet, rssi = rssi, receivedAt = now)
                _received.tryEmit(receivedSos)
            }
        }
    }

    private fun scheduleRelay(packet: SosPacket) {
        val relayedPacket = packet.withDecrementedTtl()
        val delayMs = kotlin.random.Random.nextLong(
            Constants.MESH_RELAY_DELAY_MIN_MS,
            Constants.MESH_RELAY_DELAY_MAX_MS + 1
        )
        BleDiagnosticsTracker.recordEvent("NARI_BLE_RELAY", "RELAY ttl=${relayedPacket.ttl}")
        BleDiagnosticsTracker.incrementRelayed()

        externalScope.launch {
            delay(delayMs)
            relayHandler?.invoke(relayedPacket)
        }
    }

    private fun processLocalAlert(packet: SosPacket, rssi: Int, now: Long) {
        // Clean up per-sender cooldown map periodically
        if (senderLastSeenMap.size > 200) {
            senderLastSeenMap.entries.removeIf { now - it.value > 180_000L }
        }

        // Rate-limiting for local distress alerts (does NOT affect mesh relay forwarding)
        if (!packet.isAck && !packet.isTest) {
            // Global alert rate limit
            val windowStart = now - Constants.GLOBAL_ALERT_RATE_LIMIT_WINDOW_MS
            while (globalAlertTimestamps.peek()?.let { it < windowStart } == true) {
                globalAlertTimestamps.poll()
            }
            if (globalAlertTimestamps.size >= Constants.GLOBAL_ALERT_RATE_LIMIT_MAX_PER_MINUTE) {
                BleDiagnosticsTracker.incrementRejectedPackets()
                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_PACKET_REJECTED",
                    "Global alert rate limit reached (${Constants.GLOBAL_ALERT_RATE_LIMIT_MAX_PER_MINUTE}/min). Suppressed #${packet.senderId}"
                )
                return
            }

            // Per-sender cooldown (10s)
            val lastSeenForSender = senderLastSeenMap[packet.senderId]
            if (lastSeenForSender != null && (now - lastSeenForSender < Constants.PACKET_RATE_LIMIT_MS)) {
                BleDiagnosticsTracker.incrementRejectedPackets()
                return
            }

            globalAlertTimestamps.add(now)
            senderLastSeenMap[packet.senderId] = now
        }

        BleDiagnosticsTracker.incrementPacketsReceived()
        val tag = if (packet.isTest) "NARI_BLE_TEST_RECEIVED" else "NARI_BLE_PACKET_RECEIVED"
        BleDiagnosticsTracker.recordEvent(
            tag,
            "RX sender=${packet.senderId} ttl=${packet.ttl} rssi=$rssi"
        )

        Log.i(TAG, "Delivered SOS alert from sender ${packet.senderId}, RSSI: $rssi dBm, TTL: ${packet.ttl}")
        val receivedSos = ReceivedSos(packet = packet, rssi = rssi, receivedAt = now)
        _received.tryEmit(receivedSos)
    }

    /**
     * Resilient raw AD structure parser.
     * Walks byte-by-byte through raw advertisement array looking for 16-bit Service Data (0x16)
     * with UUID 0xFDE1, Manufacturer Data (0xFF), or magic byte header 0x53 0x01.
     */
    private fun extractPayloadFromRawBytes(raw: ByteArray): ByteArray? {
        var index = 0
        while (index < raw.size) {
            val length = raw[index].toInt() and 0xFF
            if (length == 0) break // 0 indicates end of AD structures
            if (index + 1 + length > raw.size) break

            val type = raw[index + 1].toInt() and 0xFF
            val dataStartIndex = index + 2
            val dataLength = length - 1

            // 0x16 = Service Data (16-bit UUID)
            if (type == 0x16 && dataLength >= 2 + PacketCodec.LEGACY_PACKET_SIZE_BYTES) {
                val uuid0 = raw[dataStartIndex].toInt() and 0xFF
                val uuid1 = raw[dataStartIndex + 1].toInt() and 0xFF
                // 0xFDE1 is [0xE1, 0xFD] in little-endian
                if (uuid0 == 0xE1 && uuid1 == 0xFD) {
                    val payload = ByteArray(dataLength - 2)
                    System.arraycopy(raw, dataStartIndex + 2, payload, 0, payload.size)
                    return payload
                }
            } else if (type == 0xFF && dataLength >= 2 + PacketCodec.LEGACY_PACKET_SIZE_BYTES) {
                // 0xFF = Manufacturer Data
                val company0 = raw[dataStartIndex].toInt() and 0xFF
                val company1 = raw[dataStartIndex + 1].toInt() and 0xFF
                if (company0 == 0xFF && company1 == 0xFF) {
                    val payload = ByteArray(dataLength - 2)
                    System.arraycopy(raw, dataStartIndex + 2, payload, 0, payload.size)
                    return payload
                }
            }
            index += 1 + length
        }

        // Direct pattern fallback: scan for magic byte 0x53 ('S') followed by version 0x01 or 0x02
        for (i in 0..(raw.size - PacketCodec.PACKET_SIZE_BYTES)) {
            if (raw[i] == PacketCodec.MAGIC_BYTE &&
                (raw[i + 1] == PacketCodec.PROTOCOL_VERSION_V1 || raw[i + 1] == PacketCodec.PROTOCOL_VERSION_V2)
            ) {
                val candidate = ByteArray(PacketCodec.PACKET_SIZE_BYTES)
                System.arraycopy(raw, i, candidate, 0, candidate.size)
                return candidate
            }
        }
        return null
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (isScanning) {
            Log.d(TAG, "BLE scan already active")
            return
        }

        if (!PermissionHelper.hasBluetoothScanPermission(context)) {
            val msg = "Missing BLUETOOTH_SCAN permission"
            BleDiagnosticsTracker.recordEvent("NARI_BLE_SCAN_FAILURE", msg, isError = true)
            Log.w(TAG, msg)
            return
        }

        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            val msg = "Bluetooth is disabled or unavailable for scanning"
            BleDiagnosticsTracker.recordEvent("NARI_BLE_SCAN_FAILURE", msg, isError = true)
            Log.w(TAG, msg)
            return
        }

        scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            val msg = "BLE Scanner not available on this device"
            BleDiagnosticsTracker.recordEvent("NARI_BLE_SCAN_FAILURE", msg, isError = true)
            Log.e(TAG, msg)
            return
        }

        fallbackScanAttempted = false

        if (BleDiagnosticsTracker.debugScanModeEnabled.value) {
            startScanWithoutFilters()
        } else {
            startScanWithFilters()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScanWithFilters() {
        // Safe filters: only Service UUIDs (never empty manufacturer data)
        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(Constants.SOS_PARCEL_UUID).build(),
            ScanFilter.Builder().setServiceUuid(ParcelUuid(Constants.SOS_LEGACY_128_UUID)).build()
        )

        val settingsBuilder = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            settingsBuilder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        }

        try {
            scanner?.startScan(filters, settingsBuilder.build(), scanCallback)
            isScanning = true
            BleDiagnosticsTracker.setScanning(true)
            BleDiagnosticsTracker.recordEvent("NARI_BLE_SCAN_START", "BLE Scanner started with Service UUID filters (FDE1)")
            Log.i(TAG, "BLE Scanner started with multi-payload filters")
        } catch (e: Exception) {
            BleDiagnosticsTracker.recordEvent("NARI_BLE_SCAN_FAILURE", "Hardware filter start failed: ${e.message}. Falling back...", isError = true)
            startScanWithoutFilters()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScanWithoutFilters() {
        val settingsBuilder = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)

        try {
            scanner?.startScan(emptyList(), settingsBuilder.build(), scanCallback)
            isScanning = true
            BleDiagnosticsTracker.setScanning(true)
            BleDiagnosticsTracker.recordEvent("NARI_BLE_SCAN_START", "BLE Scanner active in Debug Mode (software filtering enabled)")
            Log.i(TAG, "BLE Scanner started without hardware filters (Debug software filtering)")
        } catch (e: Exception) {
            isScanning = false
            BleDiagnosticsTracker.setScanning(false)
            BleDiagnosticsTracker.recordEvent("NARI_BLE_SCAN_FAILURE", "Software filter scan failed: ${e.message}", isError = true)
            Log.e(TAG, "Scan without filters failed", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (!isScanning) return
        try {
            scanner?.stopScan(scanCallback)
            BleDiagnosticsTracker.recordEvent("NARI_BLE_SCAN_START", "BLE Scanner stopped")
            Log.i(TAG, "BLE Scanner stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Exception stopping BLE scan", e)
        } finally {
            isScanning = false
            BleDiagnosticsTracker.setScanning(false)
        }
    }

    fun restartScanning() {
        stopScanning()
        startScanning()
    }

    companion object {
        private const val TAG = "BleScanner"
    }
}
