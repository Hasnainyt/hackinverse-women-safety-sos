package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.ble.PacketCodec
import com.brokencoders.narisuraksha.core.SosPacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PacketCodecTest {

    @Test
    fun testEncodeDecodeRoundTrip() {
        val original = SosPacket(
            senderId = 12345.toShort(),
            timestamp = 1717171717,
            lat = 28.613939f,
            lon = 77.209023f,
            flags = SosPacket.FLAG_SOS.toByte()
        )

        val encoded = PacketCodec.encode(original)!!
        // 1 magic + 1 version + 2 sender + 4 timestamp + 4 lat + 4 lon + 1 flags = 17 bytes
        assertEquals(17, encoded.size)
        assertEquals(0x53.toByte(), encoded[0]) // Magic byte 'S'
        assertEquals(PacketCodec.PROTOCOL_VERSION, encoded[1]) // Protocol version 0x02

        val decoded = PacketCodec.decode(encoded)
        assertNotNull(decoded)
        assertEquals(original.senderId, decoded!!.senderId)
        assertEquals(original.timestamp, decoded.timestamp)
        assertEquals(original.lat, decoded.lat, 0.0001f)
        assertEquals(original.lon, decoded.lon, 0.0001f)
        assertEquals(original.flags, decoded.flags)
        assertEquals(original.ttl, decoded.ttl)
        assertTrue(decoded.isSos)
    }

    @Test
    fun testTtlEncodeDecodeRoundTrip() {
        val original = SosPacket(
            senderId = 777.toShort(),
            timestamp = 1800000000,
            lat = 19.0760f,
            lon = 72.8777f,
            flags = SosPacket.FLAG_SOS.toByte(),
            ttl = 3
        )

        val encoded = PacketCodec.encode(original)!!
        assertEquals(17, encoded.size)
        // Verify upper 4 bits are 3 (0x30) and lower 4 bits are FLAG_SOS (0x01) -> 0x31
        val expectedFlagsByte = ((3 shl 4) or SosPacket.FLAG_SOS).toByte()
        assertEquals(expectedFlagsByte, encoded[16])

        val decoded = PacketCodec.decode(encoded)
        assertNotNull(decoded)
        assertEquals(3, decoded!!.ttl)
        assertEquals(SosPacket.FLAG_SOS.toByte(), decoded.flags)
        assertTrue(decoded.isSos)
    }

    @Test
    fun testLegacyVersion1PacketDecodesWithTtlZero() {
        // Construct 17-byte v1 packet with version 0x01
        val v1Buffer = java.nio.ByteBuffer.allocate(17)
        v1Buffer.order(java.nio.ByteOrder.BIG_ENDIAN)
        v1Buffer.put(PacketCodec.MAGIC_BYTE)
        v1Buffer.put(PacketCodec.PROTOCOL_VERSION_V1) // 0x01
        v1Buffer.putShort(456.toShort())
        v1Buffer.putInt(1700000000)
        v1Buffer.putFloat(28.6139f)
        v1Buffer.putFloat(77.2090f)
        v1Buffer.put(SosPacket.FLAG_SOS.toByte())

        val decoded = PacketCodec.decode(v1Buffer.array())
        assertNotNull(decoded)
        assertEquals(456.toShort(), decoded!!.senderId)
        assertEquals(0, decoded.ttl) // v1 packets default to TTL 0
        assertTrue(decoded.isSos)
    }

    @Test
    fun testRelayedCopyHelper() {
        val original = SosPacket.create(
            senderId = 111.toShort(),
            lat = 28.5,
            lon = 77.2,
            ttl = 4
        )
        assertEquals(4, original.ttl)
        assertEquals(0, original.hopCount)

        val hop1 = original.relayedCopy()
        assertNotNull(hop1)
        assertEquals(3, hop1!!.ttl)
        assertEquals(1, hop1.hopCount)

        val hop2 = hop1.relayedCopy()
        assertNotNull(hop2)
        assertEquals(2, hop2!!.ttl)
        assertEquals(2, hop2.hopCount)

        val hop3 = hop2.relayedCopy()
        assertNotNull(hop3)
        assertEquals(1, hop3!!.ttl)
        assertEquals(3, hop3.hopCount)

        val hop4 = hop3.relayedCopy()
        assertNotNull(hop4)
        assertEquals(0, hop4!!.ttl)
        assertEquals(4, hop4.hopCount)

        // TTL 0 cannot be relayed further
        val hop5 = hop4.relayedCopy()
        org.junit.Assert.assertNull(hop5)
    }

    @Test
    fun testWithDecrementedTtlNeverGoesBelowZero() {
        val packetTtl0 = SosPacket.create(senderId = 1.toShort(), ttl = 0)
        assertEquals(0, packetTtl0.ttl)
        val decremented = packetTtl0.withDecrementedTtl()
        assertEquals(0, decremented.ttl) // Clamped to 0
    }

    @Test
    fun testFlagBitsUnaffectedByTtl() {
        val testTtls = listOf(0, 1, 4, 7, 15)
        for (ttlVal in testTtls) {
            // SOS with location unavailable
            val p1 = SosPacket.create(senderId = 10.toShort(), lat = null, lon = null, ttl = ttlVal)
            val enc1 = PacketCodec.encode(p1)!!
            val dec1 = PacketCodec.decode(enc1)!!
            assertTrue("SOS flag must be set for TTL=$ttlVal", dec1.isSos)
            assertTrue("Loc unavailable flag must be set for TTL=$ttlVal", dec1.isLocationUnavailable)
            assertFalse("ACK flag must NOT be set for TTL=$ttlVal", dec1.isAck)
            assertFalse("Test flag must NOT be set for TTL=$ttlVal", dec1.isTest)
            assertEquals(ttlVal, dec1.ttl)

            // ACK
            val p2 = SosPacket.create(senderId = 20.toShort(), isAck = true, targetSenderId = 10.toShort(), ttl = ttlVal)
            val enc2 = PacketCodec.encode(p2)!!
            val dec2 = PacketCodec.decode(enc2)!!
            assertFalse(dec2.isSos)
            assertTrue("ACK flag must be set for TTL=$ttlVal", dec2.isAck)
            assertEquals(10.toShort(), dec2.targetSenderId)
            assertEquals(ttlVal, dec2.ttl)

            // TEST
            val p3 = SosPacket.create(senderId = 30.toShort(), isTest = true, ttl = ttlVal)
            val enc3 = PacketCodec.encode(p3)!!
            val dec3 = PacketCodec.decode(enc3)!!
            assertFalse(dec3.isSos)
            assertFalse(dec3.isAck)
            assertTrue("Test flag must be set for TTL=$ttlVal", dec3.isTest)
            assertEquals(ttlVal, dec3.ttl)
        }
    }

    @Test
    fun testLegacy15ByteDecodeFallback() {
        // Construct raw 15-byte packet without magic/version header
        val legacyBuffer = java.nio.ByteBuffer.allocate(15)
        legacyBuffer.order(java.nio.ByteOrder.BIG_ENDIAN)
        legacyBuffer.putShort(999.toShort())
        legacyBuffer.putInt(1650000000)
        legacyBuffer.putFloat(12.34f)
        legacyBuffer.putFloat(56.78f)
        legacyBuffer.put(SosPacket.FLAG_SOS.toByte())

        val decoded = PacketCodec.decode(legacyBuffer.array())
        assertNotNull(decoded)
        assertEquals(999.toShort(), decoded!!.senderId)
        assertEquals(1650000000, decoded.timestamp)
        assertEquals(12.34f, decoded.lat, 0.0001f)
        assertEquals(56.78f, decoded.lon, 0.0001f)
        assertTrue(decoded.isSos)
    }

    @Test
    fun testAckTargetSenderId() {
        val ackPacket = SosPacket.create(
            senderId = 555.toShort(),
            lat = null,
            lon = null,
            isAck = true,
            targetSenderId = 12345.toShort()
        )

        val encoded = PacketCodec.encode(ackPacket)
        val decoded = PacketCodec.decode(encoded)
        assertNotNull(decoded)
        assertTrue(decoded!!.isAck)
        assertEquals(555.toShort(), decoded.senderId)
        assertEquals(12345.toShort(), decoded.targetSenderId)
    }

    @Test
    fun testLocationUnavailableFlag() {
        val packet = SosPacket.create(
            senderId = (-4321).toShort(),
            lat = null,
            lon = null,
            timestampSeconds = 1600000000
        )

        val encoded = PacketCodec.encode(packet)!!
        assertEquals(17, encoded.size)
        val decoded = PacketCodec.decode(encoded)

        assertNotNull(decoded)
        assertTrue(decoded!!.isLocationUnavailable)
        assertTrue(decoded.isSos)
        assertEquals(0.0f, decoded.lat, 0.0001f)
        assertEquals(0.0f, decoded.lon, 0.0001f)
    }

    @Test
    fun testTestSosPacketFlag() {
        val testPacket = SosPacket.create(
            senderId = 9876.toShort(),
            lat = 28.6139,
            lon = 77.2090,
            isTest = true
        )

        org.junit.Assert.assertTrue(testPacket.isTest)
        org.junit.Assert.assertFalse(testPacket.isSos)
        org.junit.Assert.assertFalse(testPacket.isAck)

        val encoded = PacketCodec.encode(testPacket)!!
        assertEquals(17, encoded.size)
        val decoded = PacketCodec.decode(encoded)

        assertNotNull(decoded)
        org.junit.Assert.assertTrue(decoded!!.isTest)
        org.junit.Assert.assertFalse(decoded.isSos)
        org.junit.Assert.assertFalse(decoded.isAck)
        assertEquals(9876.toShort(), decoded.senderId)
        assertEquals(28.6139f, decoded.lat, 0.0001f)
        assertEquals(77.2090f, decoded.lon, 0.0001f)
    }

    @Test
    fun testSafeZonesDistanceCalculation() {
        // Distance to the exact same point should be 0.0
        val distZero = com.brokencoders.narisuraksha.data.SafeZonesRepository.calculateDistanceKm(
            28.6139, 77.2090,
            28.6139, 77.2090
        )
        assertEquals(0.0, distZero, 0.001)

        // Distance between CP (28.6304, 77.2177) and India Gate (28.6129, 77.2295) is approx 2.2 km
        val distCpToGate = com.brokencoders.narisuraksha.data.SafeZonesRepository.calculateDistanceKm(
            28.6304, 77.2177,
            28.6129, 77.2295
        )
        assertTrue(distCpToGate in 1.8..2.6)
    }

    @Test
    fun testManualHexBytesAndFlagsDecode() {
        val packet = SosPacket(
            senderId = 0x1234.toShort(),
            timestamp = 0x56789ABC,
            lat = 12.9716f,
            lon = 77.5946f,
            flags = SosPacket.FLAG_SOS.toByte(),
            ttl = 4
        )

        val encoded = PacketCodec.encode(packet)!!
        val hexString = encoded.joinToString("") { String.format("%02X", it) }
        println("Encoded v2 packet hex: $hexString")

        // Assert length is exactly 17 bytes
        assertEquals(17, encoded.size)
        // Assert Magic byte is 'S' (0x53)
        assertEquals(0x53.toByte(), encoded[0])
        // Assert Version is 0x02
        assertEquals(0x02.toByte(), encoded[1])
        // Assert Flags byte at index 16 is 0x41 (TTL 4 in upper nibble = 0x40 | SOS 0x01 = 0x41)
        assertEquals(0x41.toByte(), encoded[16])

        // Decode and verify
        val decoded = PacketCodec.decode(encoded)
        assertNotNull(decoded)
        assertEquals(4, decoded!!.ttl)
        assertTrue(decoded.isSos)
        assertEquals(0x1234.toShort(), decoded.senderId)
    }

    @Test
    fun testPreChangeCapturedV1PacketDecodes() {
        val latBytes = java.nio.ByteBuffer.allocate(4).putFloat(12.5f).array()
        val lonBytes = java.nio.ByteBuffer.allocate(4).putFloat(77.5f).array()

        val v1Bytes = byteArrayOf(
            0x53, 0x01,
            0x04, 0xD2.toByte(),
            0x66, 0x55, 0x44, 0x33,
            latBytes[0], latBytes[1], latBytes[2], latBytes[3],
            lonBytes[0], lonBytes[1], lonBytes[2], lonBytes[3],
            0x01 // SOS flag, no TTL in v1
        )

        assertEquals(17, v1Bytes.size)
        val decoded = PacketCodec.decode(v1Bytes)
        assertNotNull(decoded)
        assertEquals(1234.toShort(), decoded!!.senderId)
        assertTrue(decoded.isSos)
        assertEquals(0, decoded.ttl) // Decoded as TTL 0 for backward compatibility
    }
}
