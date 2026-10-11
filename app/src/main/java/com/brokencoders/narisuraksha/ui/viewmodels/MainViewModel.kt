package com.brokencoders.narisuraksha.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import com.brokencoders.narisuraksha.R
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brokencoders.narisuraksha.ble.BleAdvertiser
import com.brokencoders.narisuraksha.ble.BleDiagnosticsTracker
import com.brokencoders.narisuraksha.ble.BleScanner
import com.brokencoders.narisuraksha.ble.BleTransport
import com.brokencoders.narisuraksha.ble.ReceivedSos
import com.brokencoders.narisuraksha.capture.Coordinates
import com.brokencoders.narisuraksha.capture.LocationFix
import com.brokencoders.narisuraksha.capture.LocationProvider
import com.brokencoders.narisuraksha.core.DeviceIdProvider
import com.brokencoders.narisuraksha.core.PermissionHelper
import com.brokencoders.narisuraksha.core.SosPacket
import com.brokencoders.narisuraksha.data.EmergencyContactDao
import com.brokencoders.narisuraksha.data.EmergencyContactEntity
import com.brokencoders.narisuraksha.data.UserPreferencesRepository
import com.brokencoders.narisuraksha.service.ScanForegroundService
import com.brokencoders.narisuraksha.service.SosForegroundService
import com.brokencoders.narisuraksha.trigger.ShakeDetector
import com.brokencoders.narisuraksha.trigger.SosEvent
import com.brokencoders.narisuraksha.trigger.SosManager
import com.brokencoders.narisuraksha.wifi.WifiMeshTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val context: Context,
    private val sosManager: SosManager,
    private val bleTransport: BleTransport,
    private val shakeDetector: ShakeDetector,
    private val preferencesRepository: UserPreferencesRepository,
    private val deviceIdProvider: DeviceIdProvider,
    private val emergencyContactDao: EmergencyContactDao,
    private val locationProvider: LocationProvider,
    private val bleAdvertiser: BleAdvertiser,
    private val bleScanner: BleScanner,
    private val wifiMesh: WifiMeshTransport? = null
) : ViewModel() {

    val isCountingDown: StateFlow<Boolean> = sosManager.isCountingDown
    val countdownSeconds: StateFlow<Int> = sosManager.countdownSeconds
    val isSosActive: StateFlow<Boolean> = sosManager.isSosActive

    val isShakeEnabled: StateFlow<Boolean> = preferencesRepository.isShakeDetectionEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isScanEnabled: StateFlow<Boolean> = preferencesRepository.isGuardianScanEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isDecoyEnabled: StateFlow<Boolean> = preferencesRepository.isDecoyModeEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val secretDecoyCode: StateFlow<String> = preferencesRepository.secretDecoyCode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "1234")

    val isOnboardingCompleted: StateFlow<Boolean> = preferencesRepository.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val anonymousDeviceId: Short get() = deviceIdProvider.deviceId

    // Location Telemetry (continuous high-accuracy fixes with honest precision)
    val currentCoordinates: StateFlow<Coordinates?> = locationProvider.currentCoordinates
    val latestFix: StateFlow<LocationFix?> = locationProvider.latestFix

    // Emergency Contacts from Room
    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> = emergencyContactDao.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val primaryContact: StateFlow<EmergencyContactEntity?> = emergencyContactDao.getPrimaryContact()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // BLE Diagnostics
    val bleDiagnostics = BleDiagnosticsTracker

    // Shake Diagnostics
    val shakeDiagnostics: StateFlow<com.brokencoders.narisuraksha.trigger.ShakeDiagnostics> = shakeDetector.diagnostics

    private val _latestReceivedAlert = MutableStateFlow<ReceivedSos?>(null)
    val latestReceivedAlert: StateFlow<ReceivedSos?> = _latestReceivedAlert.asStateFlow()

    /** Consumes the sticky incoming-alert banner after navigation to Alert. */
    fun clearReceivedAlert() {
        _latestReceivedAlert.value = null
    }

    private val _responderAckCount = MutableStateFlow(0)
    val responderAckCount: StateFlow<Int> = _responderAckCount.asStateFlow()
    // Tracks unique responder IDs who have acknowledged this device's current SOS beacon
    private val acknowledgedResponders = mutableSetOf<Short>()

    // Tracks ACKs WE sent recently, keyed by targetSenderId — dedupes retransmitted
    // SOS packets so the BLE advertising queue doesn't get flooded with identical ACKs.
    private val recentAckTargets = mutableMapOf<Short, Long>()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _isSirenOn = MutableStateFlow(false)
    val isSirenOn: StateFlow<Boolean> = _isSirenOn.asStateFlow()

    private val _isTestBroadcasting = MutableStateFlow(false)
    val isTestBroadcasting: StateFlow<Boolean> = _isTestBroadcasting.asStateFlow()

    private var alarmRingtone: Ringtone? = null

    init {
        // Collect SOS confirmation to launch SosForegroundService
        viewModelScope.launch {
            sosManager.events.collectLatest { event ->
                when (event) {
                    is SosEvent.CountdownStarted -> {
                        Log.i(TAG, "SosEvent: Countdown started")
                        acknowledgedResponders.clear()
                        _responderAckCount.value = 0
                    }
                    is SosEvent.Cancelled -> {
                        Log.i(TAG, "SosEvent: SOS Cancelled")
                        acknowledgedResponders.clear()
                        _responderAckCount.value = 0
                        SosForegroundService.stop(context)
                    }
                    is SosEvent.Confirmed -> {
                        Log.i(TAG, "SosEvent: SOS Confirmed! Launching broadcast service...")
                        acknowledgedResponders.clear()
                        _responderAckCount.value = 0
                        SosForegroundService.start(
                            context = context,
                            lat = event.lat,
                            lon = event.lon,
                            audioPath = event.audioPath
                        )
                    }
                }
            }
        }

        // Collect incoming BLE alerts & ACKs
        viewModelScope.launch {
            bleTransport.received.collectLatest { receivedSos ->
                if (receivedSos.packet.isAck) {
                    val targetId = receivedSos.packet.targetSenderId
                    val myDeviceId = deviceIdProvider.deviceId
                    // Only count ACK if addressed to our device (or 0 for legacy broadcast ACK)
                    if (targetId == 0.toShort() || targetId == myDeviceId) {
                        val responderId = receivedSos.packet.senderId
                        if (acknowledgedResponders.add(responderId)) {
                            Log.i(TAG, "Distinct responder #$responderId confirmed distress. Total: ${acknowledgedResponders.size}")
                            _responderAckCount.value = acknowledgedResponders.size
                        } else {
                            Log.d(TAG, "Duplicate ACK from responder #$responderId ignored")
                        }
                    } else {
                        Log.d(TAG, "Ignoring ACK intended for sender #$targetId (my deviceId is #$myDeviceId)")
                    }
                } else {
                    _latestReceivedAlert.value = receivedSos
                }
            }
        }

        // Collect shake events to trigger SOS if shake detection is enabled
        viewModelScope.launch {
            shakeDetector.shakeEvents.collectLatest {
                if (isShakeEnabled.value) {
                    Log.i(TAG, "Shake event received! Triggering SOS countdown...")
                    triggerSos()
                }
            }
        }

        // Sync shake detector state
        viewModelScope.launch {
            preferencesRepository.isShakeDetectionEnabled.collectLatest { enabled ->
                if (enabled) {
                    shakeDetector.start()
                } else {
                    shakeDetector.stop()
                }
            }
        }

        // Fetch location on startup
        refreshLocation()
    }

    /**
     * Proactively verifies that background guardian scan is running if user has enabled it.
     */
    fun ensureGuardianScanActive() {
        // WiFi mesh listener is permission-free (normal WiFi perms) and idles
        // when unjoined — start it directly alongside the BLE guardian.
        try {
            wifiMesh?.start()
        } catch (e: Exception) {
            Log.w(TAG, "Could not start WiFi mesh listener: ${e.message}")
        }
        if (isScanEnabled.value && PermissionHelper.hasBluetoothScanPermission(context)) {
            try {
                ScanForegroundService.start(context)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start ScanForegroundService: ${e.message}")
            }
        }
    }

    fun refreshLocation() {
        try {
            locationProvider.startTracking()
        } catch (e: Exception) {
            Log.w(TAG, "Location tracking start failed: ${e.message}")
        }
        viewModelScope.launch {
            locationProvider.getCurrentLocation()
        }
    }

    fun triggerSos() {
        acknowledgedResponders.clear()
        _responderAckCount.value = 0
        sosManager.triggerSos()
    }

    fun cancelCountdown() {
        acknowledgedResponders.clear()
        _responderAckCount.value = 0
        sosManager.cancelCountdown()
    }

    fun stopSos() {
        acknowledgedResponders.clear()
        _responderAckCount.value = 0
        sosManager.stopSos()
        SosForegroundService.stop(context)

        // Standing down silences the deterrents too: a victim tapping
        // "I'm Safe" must never leave a blaring siren or torch behind.
        if (_isSirenOn.value) {
            toggleSiren()
        }
        if (_isFlashlightOn.value) {
            toggleFlashlight()
        }

        // BleAdvertiser internally flushes any deferred ACKs when the SOS slot frees up.
        BleDiagnosticsTracker.recordEvent(
            "NARI_BLE_ACK_DRAINED",
            "SOS stopped — deferred ACKs (if any) will flush via BleAdvertiser"
        )
    }

    /**
     * Sends an ACK beacon addressed to [targetSenderId].
     *
     * Deduped via [recentAckTargets] because a single SOS advertisement can be
     * received multiple times as the chipset retransmits it. Without dedup, the
     * BleAdvertiser pending-ACK queue would fill with identical entries.
     *
     * Routed through [bleAdvertiser] (not bleTransport) so the SOS-priority guard
     * applies — an active SOS broadcast will NOT be replaced by this ACK.
     */
    fun sendResponderAck(targetSenderId: Short) {
        val now = System.currentTimeMillis()

        val lastAck = recentAckTargets[targetSenderId]
        if (lastAck != null && now - lastAck < ACK_COOLDOWN_MS) {
            Log.d(TAG, "ACK to #$targetSenderId skipped (cooldown ${now - lastAck}ms)")
            return
        }
        recentAckTargets[targetSenderId] = now

        // Opportunistic pruning so the map doesn't grow unbounded
        try {
            recentAckTargets.entries.removeAll { now - it.value > ACK_COOLDOWN_MS * 4 }
        } catch (_: Exception) {
            // ignore concurrent pruning failures; map is best-effort dedup
        }

        Log.i(TAG, "Sending responder ACK beacon targeting sender #$targetSenderId")

        val ackPacket = SosPacket.create(
            senderId = deviceIdProvider.deviceId,
            lat = null,
            lon = null,
            isAck = true,
            targetSenderId = targetSenderId
        )

        try {
            bleAdvertiser.startAdvertising(ackPacket, timeoutMs = ACK_ADVERTISE_TIMEOUT_MS)
        } catch (e: SecurityException) {
            Log.e(TAG, "ACK not sent: missing BLUETOOTH_ADVERTISE permission", e)
            return
        } catch (e: IllegalStateException) {
            Log.e(TAG, "ACK not sent: Bluetooth off", e)
            return
        }

        BleDiagnosticsTracker.recordEvent(
            "NARI_BLE_ACK_SENT",
            "ACK queued/dispatched for target #$targetSenderId"
        )
    }

    /**
     * Starts a harmless 10-second test broadcast flagged with FLAG_TEST.
     * Displays in nearby debug screens without triggering sirens or emergency workflows.
     */
    fun startTestBroadcast() {
        if (_isTestBroadcasting.value) return
        _isTestBroadcasting.value = true

        val fix = latestFix.value
        val coords = fix?.toCoordinates() ?: currentCoordinates.value
        val testPacket = SosPacket.create(
            senderId = deviceIdProvider.deviceId,
            lat = coords?.lat,
            lon = coords?.lon,
            isTest = true
        )

        bleDiagnostics.recordEvent(
            "NARI_BLE_ADVERTISE_START",
            "Broadcasting 10-second TEST beacon (FLAG_TEST set)"
        )
        try {
            bleAdvertiser.startAdvertising(testPacket, timeoutMs = 10_000L)
        } catch (e: SecurityException) {
            Log.e(TAG, "Test broadcast blocked: missing BLUETOOTH_ADVERTISE permission", e)
            _isTestBroadcasting.value = false
            return
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Test broadcast blocked: Bluetooth off", e)
            _isTestBroadcasting.value = false
            return
        }

        viewModelScope.launch {
            kotlinx.coroutines.delay(10_000L)
            _isTestBroadcasting.value = false
        }
    }

    fun stopTestBroadcast() {
        bleAdvertiser.stopAdvertising()
        _isTestBroadcasting.value = false
    }

    fun restartBleScan() {
        try {
            bleScanner.restartScanning()
        } catch (e: SecurityException) {
            Log.e(TAG, "BLE rescan blocked: missing permissions", e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "BLE rescan blocked: Bluetooth off", e)
        }
    }

    fun toggleDebugScanMode(enabled: Boolean) {
        bleDiagnostics.setDebugScanMode(enabled)
        bleScanner.restartScanning()
    }

    /**
     * Injects a synthetic incoming SOS packet for emulator validation.
     */
    fun injectSimulatedSosPacket() {
        val simulatedSenderId = ((deviceIdProvider.deviceId.toInt() + 101) and 0x7FFF).toShort()
        val packet = SosPacket(
            senderId = simulatedSenderId,
            timestamp = (System.currentTimeMillis() / 1000).toInt(),
            lat = 28.6139f,
            lon = 77.2090f,
            flags = SosPacket.FLAG_SOS.toByte(),
            ttl = 3 // 1 hop via mesh relay
        )
        bleScanner.injectSimulatedPacket(packet)
    }

    /**
     * Injects a simulated SOS packet into the UI and BLE diagnostics for emulator/demo validation.
     * Safe for debug environments where a second physical transmitter is unavailable.
     */
    fun injectSimulatedSosForTesting(
        senderId: Short = 999,
        lat: Float = 28.6139f,
        lon: Float = 77.2090f,
        rssi: Int = -62
    ) {
        val packet = SosPacket.create(
            senderId = senderId,
            lat = lat.toDouble(),
            lon = lon.toDouble()
        )
        _latestReceivedAlert.value = ReceivedSos(packet = packet, rssi = rssi, receivedAt = System.currentTimeMillis())
        bleDiagnostics.recordEvent("SIMULATION", "Injected simulated SOS beacon from sender #$senderId (RSSI: $rssi dBm)")
        bleDiagnostics.incrementPacketsReceived()
    }

    // ==========================================
    // Emergency Contacts Management
    // ==========================================

    fun addEmergencyContact(name: String, phoneNumber: String, relationship: String, isPrimary: Boolean) {
        if (name.isBlank() || phoneNumber.isBlank()) return
        viewModelScope.launch {
            val contact = EmergencyContactEntity(
                name = name.trim(),
                phoneNumber = phoneNumber.trim(),
                relationship = relationship.trim().ifEmpty { "Contact" },
                isPrimary = isPrimary
            )
            val newId = emergencyContactDao.insertContact(contact)
            if (isPrimary) {
                emergencyContactDao.setAsPrimary(newId)
            }
        }
    }

    fun updateEmergencyContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            emergencyContactDao.updateContact(contact)
            if (contact.isPrimary) {
                emergencyContactDao.setAsPrimary(contact.id)
            }
        }
    }

    fun deleteEmergencyContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            emergencyContactDao.deleteContact(contact)
        }
    }

    fun setPrimaryContact(contactId: Long) {
        viewModelScope.launch {
            emergencyContactDao.setAsPrimary(contactId)
        }
    }

    /**
     * Directly launches phone dialer intent for the contact.
     */
    fun callContact(phoneNumber: String) {
        try {
            val cleanPhone = phoneNumber.filter { it.isDigit() || it == '+' }
            if (cleanPhone.isEmpty()) {
                Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                Toast.makeText(context, "No dialer app found on this device", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Composes pre-filled emergency SOS SMS for contact.
     */
    fun sendEmergencySms(phoneNumber: String, customLocationText: String? = null) {
        try {
            val cleanPhone = phoneNumber.filter { it.isDigit() || it == '+' }
            if (cleanPhone.isEmpty()) {
                Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show()
                return
            }
            val fix = latestFix.value
            val coords = currentCoordinates.value
            val locationString = when {
                customLocationText != null -> customLocationText
                fix != null -> {
                    val accText = fix.accuracyM?.let { "±${it.toInt()} m" } ?: "accuracy unknown"
                    val ageS = fix.ageMs() / 1000
                    "Lat: ${fix.lat}, Lon: ${fix.lon} ($accText, ${ageS}s old)\n${fix.mapsLink()}"
                }
                coords != null -> {
                    val accText = coords.accuracy?.let { "±${it.toInt()} m" } ?: "accuracy unknown"
                    "Lat: ${coords.lat}, Lon: ${coords.lon} ($accText)\nhttps://maps.google.com/?q=${coords.lat},${coords.lon}"
                }
                else -> "Location unavailable (Offline — track via BLE mesh proximity)"
            }
            val time = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(Date())

            val message = """
                Nari-Suraksha SOS ALERT

                I may be in danger and need help.

                Location:
                $locationString

                Time:
                $time

                Please contact me or emergency services immediately.
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanPhone")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                Toast.makeText(context, "No SMS app found on this device", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateSecretDecoyCode(newPin: String) {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            viewModelScope.launch {
                preferencesRepository.setSecretDecoyCode(newPin)
                Log.i(TAG, "Decoy PIN updated to: $newPin")
            }
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(completed)
            if (completed) {
                ensureGuardianScanActive()
            }
        }
    }

    fun setShakeDetectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setShakeDetectionEnabled(enabled)
        }
    }

    fun setGuardianScanEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setGuardianScanEnabled(enabled)
            if (!enabled) {
                ScanForegroundService.stop(context)
                try {
                    wifiMesh?.stop()
                } catch (_: Exception) {
                }
                return@launch
            }
            if (PermissionHelper.hasBluetoothPermissions(context)) {
                ScanForegroundService.start(context)
            } else {
                Log.w(TAG, "Guardian scan not started: Bluetooth permissions missing")
            }
        }
    }

    fun setDecoyModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDecoyModeEnabled(enabled)
        }
    }

    fun toggleFlashlight() {
        try {
            // CAMERA runtime permission is required for setTorchMode on API 23+.
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.CAMERA
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Flashlight unavailable: CAMERA permission not granted")
                return
            }
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return
            // Select a back-facing camera with an actual flash unit — firstOrNull()
            // commonly returns the front camera without flash and throws.
            var flashCameraId: String? = null
            for (id in cameraManager.cameraIdList) {
                try {
                    val chars = cameraManager.getCameraCharacteristics(id)
                    val hasFlash = chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    val facing = chars.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                        flashCameraId = id
                        break
                    }
                    if (hasFlash && flashCameraId == null) {
                        flashCameraId = id
                    }
                } catch (_: Exception) {
                    continue
                }
            }
            val cameraId = flashCameraId ?: return
            val newState = !_isFlashlightOn.value
            cameraManager.setTorchMode(cameraId, newState)
            _isFlashlightOn.value = newState
        } catch (e: SecurityException) {
            Log.e(TAG, "Flashlight denied: missing CAMERA permission", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling flashlight", e)
        }
    }

    fun toggleSiren() {
        try {
            val newState = !_isSirenOn.value
            if (newState) {
                alarmRingtone?.stop()
                alarmRingtone = null
                val alarmUri = Uri.parse("android.resource://${context.packageName}/${R.raw.siren}")

                alarmRingtone = RingtoneManager.getRingtone(context, alarmUri)?.apply {
                    audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    play()
                }
                _isSirenOn.value = true
            } else {
                alarmRingtone?.stop()
                alarmRingtone = null
                _isSirenOn.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling siren", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            alarmRingtone?.stop()
        } catch (_: Exception) {
            // ignore stop failures during teardown
        } finally {
            alarmRingtone = null
        }
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            if (_isFlashlightOn.value && cameraManager != null) {
                for (id in cameraManager.cameraIdList) {
                    try {
                        cameraManager.setTorchMode(id, false)
                        break
                    } catch (_: Exception) {
                        continue
                    }
                }
            }
        } catch (_: Exception) {
            // ignore torch teardown failures
        } finally {
            _isFlashlightOn.value = false
        }
        try {
            shakeDetector.stop()
        } catch (_: Exception) {
            // ignore sensor teardown failures
        }
        try {
            locationProvider.stopTracking()
        } catch (_: Exception) {
            // ignore tracking teardown failures
        }
    }

    companion object {
        private const val TAG = "MainViewModel"

        /** How long an ACK slot is held before auto-stopping (fire-and-forget). */
        private const val ACK_ADVERTISE_TIMEOUT_MS = 3_000L

        /** Skip sending another ACK to the same target within this window. */
        private const val ACK_COOLDOWN_MS = 3_000L

        fun provideFactory(
            context: Context,
            sosManager: SosManager,
            bleTransport: BleTransport,
            shakeDetector: ShakeDetector,
            preferencesRepository: UserPreferencesRepository,
            deviceIdProvider: DeviceIdProvider,
            emergencyContactDao: EmergencyContactDao,
            locationProvider: LocationProvider,
            bleAdvertiser: BleAdvertiser,
            bleScanner: BleScanner,
            wifiMesh: WifiMeshTransport? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(
                    context,
                    sosManager,
                    bleTransport,
                    shakeDetector,
                    preferencesRepository,
                    deviceIdProvider,
                    emergencyContactDao,
                    locationProvider,
                    bleAdvertiser,
                    bleScanner,
                    wifiMesh
                ) as T
            }
        }
    }
}