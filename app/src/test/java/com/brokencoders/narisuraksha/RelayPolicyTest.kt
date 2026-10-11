package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.ble.RelayBudgetTracker
import com.brokencoders.narisuraksha.ble.RelayDecision
import com.brokencoders.narisuraksha.ble.RelayDedupCache
import com.brokencoders.narisuraksha.ble.RelayPolicy
import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.core.SosPacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RelayPolicyTest {

    private val myDeviceId: Short = 100

    @Test
    fun testOwnPacketIgnored() {
        val cache = RelayDedupCache()
        val ownPacket = SosPacket.create(senderId = myDeviceId, ttl = 4)

        val decision = RelayPolicy.evaluate(ownPacket, myDeviceId, cache) { true }
        assertEquals(RelayDecision.IGNORE_OWN, decision)
    }

    @Test
    fun testDuplicateNotRelayed() {
        val cache = RelayDedupCache()
        val packet = SosPacket.create(senderId = 200.toShort(), ttl = 4)

        val first = RelayPolicy.evaluate(packet, myDeviceId, cache) { true }
        assertEquals(RelayDecision.ALERT_AND_RELAY, first)

        // Duplicate identical packet
        val second = RelayPolicy.evaluate(packet, myDeviceId, cache) { true }
        assertEquals(RelayDecision.DROP_DUPLICATE, second)
    }

    @Test
    fun testTestFlagNotRelayed() {
        val cache = RelayDedupCache()
        val testPacket = SosPacket.create(senderId = 300.toShort(), isTest = true, ttl = 4)

        val decision = RelayPolicy.evaluate(testPacket, myDeviceId, cache) { true }
        assertEquals("FLAG_TEST must produce ALERT_ONLY (delivered to diagnostics, never relayed)",
            RelayDecision.ALERT_ONLY, decision)
    }

    @Test
    fun testAckForMeConsumedNotRelayed() {
        val cache = RelayDedupCache()
        val ackForMe = SosPacket.create(
            senderId = 400.toShort(),
            isAck = true,
            targetSenderId = myDeviceId,
            ttl = 4
        )

        val decision = RelayPolicy.evaluate(ackForMe, myDeviceId, cache) { true }
        assertEquals(RelayDecision.CONSUME_ACK_FOR_ME, decision)
    }

    @Test
    fun testAckForOthersRelayed() {
        val cache = RelayDedupCache()
        val otherVictimId: Short = 999
        val ackForOther = SosPacket.create(
            senderId = 400.toShort(),
            isAck = true,
            targetSenderId = otherVictimId,
            ttl = 3
        )

        val decision = RelayPolicy.evaluate(ackForOther, myDeviceId, cache) { true }
        assertEquals("ACK for someone else must be RELAY_ONLY (no alert siren, mesh forwarded)",
            RelayDecision.RELAY_ONLY, decision)
    }

    @Test
    fun testTtlZeroNotRelayed() {
        val cache = RelayDedupCache()
        val packetTtl0 = SosPacket.create(senderId = 500.toShort(), ttl = 0)

        val decision = RelayPolicy.evaluate(packetTtl0, myDeviceId, cache) { true }
        assertEquals("SOS with TTL 0 must alert locally but NOT relay", RelayDecision.ALERT_ONLY, decision)
    }

    @Test
    fun testRelayBudgetRespected() {
        val cache = RelayDedupCache()
        val packet = SosPacket.create(senderId = 600.toShort(), ttl = 4)

        // When relay budget is exhausted (returns false)
        val decision = RelayPolicy.evaluate(packet, myDeviceId, cache) { false }
        assertEquals("When budget is exhausted, SOS must ALERT_ONLY", RelayDecision.ALERT_ONLY, decision)

        val ackForOther = SosPacket.create(senderId = 601.toShort(), isAck = true, targetSenderId = 888.toShort(), ttl = 4)
        val ackDecision = RelayPolicy.evaluate(ackForOther, myDeviceId, cache) { false }
        assertEquals("When budget is exhausted, ACK for other is ignored", RelayDecision.IGNORE_OWN, ackDecision)
    }

    @Test
    fun testRelayBudgetTrackerSlidingWindow() {
        var currentTime = 1000L
        val tracker = RelayBudgetTracker(maxPerMinute = 12, windowMs = 60_000L, clock = { currentTime })

        // Consume 12 budget tokens
        for (i in 1..12) {
            assertTrue("Token $i should be acquired", tracker.tryAcquire())
        }

        // 13th attempt within same minute window should fail
        assertFalse("13th token must be rejected", tracker.tryAcquire())

        // Advance clock by 61 seconds
        currentTime += 61_000L
        assertTrue("Tokens should replenish after sliding window", tracker.tryAcquire())
    }

    @Test
    fun testRateLimitedAlertStillRelayedConcept() {
        val cache = RelayDedupCache()
        val senderId: Short = 700

        // In BleScanner, rate limiting occurs on the ALERT branch, NOT the relay branch
        val packet1 = SosPacket.create(senderId = senderId, timestampSeconds = 100, ttl = 4)
        val decision1 = RelayPolicy.evaluate(packet1, myDeviceId, cache) { true }
        assertEquals(RelayDecision.ALERT_AND_RELAY, decision1)

        // Fresh packet from same sender (e.g. 5 seconds later, updated coordinates/timestamp)
        val packet2 = SosPacket.create(senderId = senderId, timestampSeconds = 105, ttl = 3)
        val decision2 = RelayPolicy.evaluate(packet2, myDeviceId, cache) { true }
        assertEquals("New packet from same sender still gets ALERT_AND_RELAY from policy",
            RelayDecision.ALERT_AND_RELAY, decision2)
    }
}
