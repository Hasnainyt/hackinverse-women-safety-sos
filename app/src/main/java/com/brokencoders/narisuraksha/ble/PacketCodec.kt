package com.brokencoders.narisuraksha.ble

import com.brokencoders.narisuraksha.core.SosPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Compact binary encoder/decoder for SosPacket.
 *
 * Versioned packet (17 bytes):
 * [0]      - Magic byte (0x53 'S')
 * [1]      - Protocol version (0x02 for Mesh TTL, 0x01 for legacy v1)
 * [2..3]   - Sender ID (Short, Big-Endian)
 * [4..7]   - Timestamp / targetSenderId (Int, Big-Endian)
 * [8..11]  - Latitude (Float, IEEE 754)
 * [12..15] - Longitude (Float, IEEE 754)
 * [16]     - Flags byte:
 *            Bits 7..4: TTL (0-15)
 *            Bits 3..0: Flags (Bit 0 SOS, Bit 1 Location Unavailable, Bit 2 ACK, Bit 3 Test)
 *
 * Legacy unversioned packet: 15 bytes.
 */
object PacketCodec {

    const val MAGIC_BYTE: Byte = 0x53
    const val PROTOCOL_VERSION_V1: Byte = 0x01
    const val PROTOCOL_VERSION_V2: Byte = 0x02
    const val PROTOCOL_VERSION: Byte = PROTOCOL_VERSION_V2

    const val PACKET_SIZE_BYTES = 17
    const val LEGACY_PACKET_SIZE_BYTES = 15

    fun encode(packet: SosPacket): ByteArray? {
        return encode(packet, PACKET_SIZE_BYTES)
    }

    fun encode(packet: SosPacket, maxSize: Int): ByteArray? {
        if (maxSize < LEGACY_PACKET_SIZE_BYTES) return null

        val buffer = ByteBuffer.allocate(PACKET_SIZE_BYTES)
            .order(ByteOrder.BIG_ENDIAN)

        buffer.put(MAGIC_BYTE)
        buffer.put(PROTOCOL_VERSION)
        buffer.putShort(packet.senderId)
        buffer.putInt(packet.timestamp)
        buffer.putFloat(packet.lat)
        buffer.putFloat(packet.lon)

        // Upper 4 bits: TTL (0-15); Lower 4 bits: Flag bitmask
        val rawFlags = (((packet.ttl and 0x0F) shl 4) or (packet.flags.toInt() and 0x0F)).toByte()
        buffer.put(rawFlags)

        val encoded = buffer.array()

        return when {
            encoded.size <= maxSize -> encoded
            maxSize >= LEGACY_PACKET_SIZE_BYTES -> {
                val legacy = ByteBuffer.allocate(LEGACY_PACKET_SIZE_BYTES)
                    .order(ByteOrder.BIG_ENDIAN)

                legacy.putShort(packet.senderId)
                legacy.putInt(packet.timestamp)
                legacy.putFloat(packet.lat)
                legacy.putFloat(packet.lon)
                legacy.put((packet.flags.toInt() and 0x0F).toByte())

                legacy.array()
            }
            else -> null
        }
    }

    fun decode(bytes: ByteArray?): SosPacket? {
        if (bytes == null) return null

        return try {
            val buffer = ByteBuffer.wrap(bytes)
                .order(ByteOrder.BIG_ENDIAN)

            when {
                bytes.size == PACKET_SIZE_BYTES && bytes[0] == MAGIC_BYTE -> {
                    buffer.get() // Magic byte
                    val version = buffer.get() // Protocol version

                    val senderId = buffer.short
                    val timestamp = buffer.int
                    val lat = buffer.float
                    val lon = buffer.float
                    val rawFlags = buffer.get()

                    val (flags, ttl) = when (version) {
                        PROTOCOL_VERSION_V2 -> {
                            val extractedTtl = (rawFlags.toInt() ushr 4) and 0x0F
                            val flagBits = (rawFlags.toInt() and 0x0F).toByte()
                            flagBits to extractedTtl
                        }
                        PROTOCOL_VERSION_V1 -> {
                            val flagBits = (rawFlags.toInt() and 0x0F).toByte()
                            flagBits to 0
                        }
                        else -> return null
                    }

                    SosPacket(
                        senderId = senderId,
                        timestamp = timestamp,
                        lat = lat,
                        lon = lon,
                        flags = flags,
                        ttl = ttl
                    )
                }

                bytes.size == LEGACY_PACKET_SIZE_BYTES -> {
                    val senderId = buffer.short
                    val timestamp = buffer.int
                    val lat = buffer.float
                    val lon = buffer.float
                    val rawFlags = buffer.get()

                    SosPacket(
                        senderId = senderId,
                        timestamp = timestamp,
                        lat = lat,
                        lon = lon,
                        flags = (rawFlags.toInt() and 0x0F).toByte(),
                        ttl = 0
                    )
                }

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Produces a relayed copy of the packet with TTL decremented by 1.
     * Returns null if TTL is already 0.
     */
    fun createRelayedPacket(packet: SosPacket): SosPacket? {
        return packet.relayedCopy()
    }
}