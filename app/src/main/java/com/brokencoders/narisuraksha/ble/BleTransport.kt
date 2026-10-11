package com.brokencoders.narisuraksha.ble

import com.brokencoders.narisuraksha.core.SosPacket
import kotlinx.coroutines.flow.Flow

data class ReceivedSos(
    val packet: SosPacket,
    val rssi: Int,
    val receivedAt: Long,
    /** Ingress medium. WiFi receptions carry no RSSI (rssi = Int.MIN_VALUE). */
    val transport: MeshTransport = MeshTransport.BLE
)

interface BleTransport {
    fun startAdvertising(packet: SosPacket)
    fun queueRelay(packet: SosPacket)
    fun stopAdvertising()
    fun startScanning()
    fun stopScanning()
    val received: Flow<ReceivedSos>
}
