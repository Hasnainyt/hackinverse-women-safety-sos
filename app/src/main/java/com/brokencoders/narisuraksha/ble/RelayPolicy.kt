package com.brokencoders.narisuraksha.ble

import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.core.SosPacket
import java.util.concurrent.ConcurrentLinkedQueue

enum class RelayDecision {
    IGNORE_OWN,
    DROP_DUPLICATE,
    CONSUME_ACK_FOR_ME,
    ALERT_ONLY,
    ALERT_AND_RELAY,
    RELAY_ONLY
}

/**
 * Thread-safe sliding-window tracker for mesh relay rate budget.
 */
class RelayBudgetTracker(
    private val maxPerMinute: Int = Constants.MESH_MAX_RELAYS_PER_MIN,
    private val windowMs: Long = 60_000L,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val timestamps = ConcurrentLinkedQueue<Long>()

    @Synchronized
    fun tryAcquire(): Boolean {
        val now = clock()
        val windowStart = now - windowMs
        while (timestamps.peek()?.let { it < windowStart } == true) {
            timestamps.poll()
        }
        return if (timestamps.size >= maxPerMinute) {
            false
        } else {
            timestamps.add(now)
            true
        }
    }
}

/**
 * Pure, deterministic relay policy engine.
 * Decides whether a packet should be dropped, consumed, alerted, or relayed along the mesh.
 */
object RelayPolicy {

    /**
     * Evaluates packet according to mesh forwarding rules in priority order:
     * a) own senderId -> ignore completely
     * b) cache.checkAndInsert false -> DROP_DUPLICATE
     * c) FLAG_TEST -> deliver to diagnostics only, never relay
     * d) ACK with targetSenderId == me -> consume, do not relay
     * e) ACK for someone else -> relay if ttl > 0, do not alert
     * f) SOS -> deliver to existing alert logic, then relay if ttl > 0
     * g) relay only if budget allows (MESH_MAX_RELAYS_PER_MIN)
     */
    fun evaluate(
        packet: SosPacket,
        myDeviceId: Short,
        cache: RelayDedupCache,
        canRelayBudget: () -> Boolean
    ): RelayDecision {
        // a) own senderId -> ignore completely
        if (packet.senderId == myDeviceId) {
            return RelayDecision.IGNORE_OWN
        }

        // b) cache.checkAndInsert false -> DROP_DUPLICATE
        if (!cache.checkAndInsert(packet)) {
            return RelayDecision.DROP_DUPLICATE
        }

        // c) FLAG_TEST -> deliver to diagnostics only, never relay
        if (packet.isTest) {
            return RelayDecision.ALERT_ONLY
        }

        // d) ACK with targetSenderId == me (or 0 broadcast ACK) -> consume, do not relay
        if (packet.isAck) {
            if (packet.targetSenderId == myDeviceId || packet.targetSenderId == 0.toShort()) {
                return RelayDecision.CONSUME_ACK_FOR_ME
            }
            // e) ACK for someone else -> relay if ttl > 0 and budget allows, do not alert
            return if (packet.ttl > 0 && canRelayBudget()) {
                RelayDecision.RELAY_ONLY
            } else {
                RelayDecision.IGNORE_OWN
            }
        }

        // f & g) SOS -> deliver to alert logic, and relay if ttl > 0 and budget allows
        val shouldRelay = packet.ttl > 0 && canRelayBudget()
        return if (shouldRelay) {
            RelayDecision.ALERT_AND_RELAY
        } else {
            RelayDecision.ALERT_ONLY
        }
    }
}
