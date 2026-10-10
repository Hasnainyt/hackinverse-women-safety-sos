package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.ble.DedupKey
import com.brokencoders.narisuraksha.ble.RelayBudgetTracker
import com.brokencoders.narisuraksha.ble.RelayDecision
import com.brokencoders.narisuraksha.ble.RelayDedupCache
import com.brokencoders.narisuraksha.ble.RelayPolicy
import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.core.SosPacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.PriorityQueue

/**
 * JVM-based discrete-event fake-radio mesh simulation test.
 * Verifies multi-hop flooding, duplicate suppression, loop termination,
 * bidirectional ACK propagation, and battery relay budget enforcement
 * across simulated mobile nodes without requiring physical Android hardware.
 */
class MeshNetworkSimulationTest {

    data class SimEvent(
        val scheduledTimeMs: Long,
        val action: () -> Unit
    )

    class SimulatedAir {
        var currentTimeMs: Long = 0L
        private val nodes = mutableMapOf<Short, SimulatedNode>()
        // adjacency[receiver] contains set of transmitters that receiver can hear
        private val canHearMatrix = mutableMapOf<Short, MutableSet<Short>>()
        private val eventQueue = PriorityQueue<SimEvent>(compareBy { it.scheduledTimeMs })

        fun registerNode(node: SimulatedNode) {
            nodes[node.deviceId] = node
            canHearMatrix.getOrPut(node.deviceId) { mutableSetOf() }
        }

        fun setBidirectionalLink(node1: Short, node2: Short) {
            canHearMatrix.getOrPut(node1) { mutableSetOf() }.add(node2)
            canHearMatrix.getOrPut(node2) { mutableSetOf() }.add(node1)
        }

        fun removeBidirectionalLink(node1: Short, node2: Short) {
            canHearMatrix[node1]?.remove(node2)
            canHearMatrix[node2]?.remove(node1)
        }

        fun schedule(delayMs: Long, action: () -> Unit) {
            eventQueue.add(SimEvent(currentTimeMs + delayMs, action))
        }

        fun transmit(transmitterId: Short, packet: SosPacket) {
            val hearers = canHearMatrix.entries
                .filter { it.value.contains(transmitterId) }
                .map { it.key }

            for (receiverId in hearers) {
                val receiver = nodes[receiverId] ?: continue
                // Radio propagation latency of 15ms in simulated air
                schedule(15L) {
                    receiver.onPacketReceived(packet)
                }
            }
        }

        fun runUntilIdle(maxEvents: Int = 10_000): Int {
            var processed = 0
            while (eventQueue.isNotEmpty() && processed < maxEvents) {
                val event = eventQueue.poll() ?: break
                currentTimeMs = event.scheduledTimeMs
                event.action()
                processed++
            }
            return processed
        }
    }

    class SimulatedNode(
        val deviceId: Short,
        private val air: SimulatedAir,
        var isResponder: Boolean = false
    ) {
        val cache = RelayDedupCache(clock = { air.currentTimeMs })
        val relayBudget = RelayBudgetTracker(clock = { air.currentTimeMs })

        var alertCount: Int = 0
        var lastReceivedSosPacket: SosPacket? = null
        var relayCount: Int = 0
        val uniqueResponders = mutableSetOf<Short>()

        init {
            air.registerNode(this)
        }

        fun sendSos(ttl: Int = Constants.MESH_DEFAULT_TTL, timestamp: Int = 1000) {
            val packet = SosPacket(
                senderId = deviceId,
                timestamp = timestamp,
                lat = 12.9716f,
                lon = 77.5946f,
                flags = SosPacket.FLAG_SOS.toByte(),
                ttl = ttl
            )
            air.transmit(deviceId, packet)
        }

        fun onPacketReceived(packet: SosPacket) {
            val decision = RelayPolicy.evaluate(
                packet = packet,
                myDeviceId = deviceId,
                cache = cache,
                canRelayBudget = { relayBudget.tryAcquire() }
            )

            when (decision) {
                RelayDecision.IGNORE_OWN, RelayDecision.DROP_DUPLICATE -> {
                    // Dropped
                }

                RelayDecision.ALERT_ONLY -> {
                    alertCount++
                    lastReceivedSosPacket = packet
                    if (isResponder && packet.isSos) {
                        sendAck(packet.senderId)
                    }
                }

                RelayDecision.ALERT_AND_RELAY -> {
                    alertCount++
                    lastReceivedSosPacket = packet
                    relayCount++
                    val relayed = packet.withDecrementedTtl()
                    // 200 - 1500 ms simulated jitter delay
                    air.schedule(250L) {
                        air.transmit(deviceId, relayed)
                    }
                    if (isResponder && packet.isSos) {
                        sendAck(packet.senderId)
                    }
                }

                RelayDecision.RELAY_ONLY -> {
                    relayCount++
                    val relayed = packet.withDecrementedTtl()
                    air.schedule(250L) {
                        air.transmit(deviceId, relayed)
                    }
                }

                RelayDecision.CONSUME_ACK_FOR_ME -> {
                    uniqueResponders.add(packet.senderId)
                }
            }
        }

        private fun sendAck(targetSenderId: Short) {
            val ack = SosPacket(
                senderId = deviceId,
                timestamp = targetSenderId.toInt(), // carries target ID
                lat = 0f,
                lon = 0f,
                flags = SosPacket.FLAG_ACK.toByte(),
                ttl = Constants.MESH_DEFAULT_TTL
            )
            air.schedule(100L) {
                air.transmit(deviceId, ack)
            }
        }
    }

    @Test
    fun testThreeNodeMeshRelayAndAckPropagation() {
        val air = SimulatedAir()
        val nodeA = SimulatedNode(1.toShort(), air, isResponder = false) // Victim
        val nodeB = SimulatedNode(2.toShort(), air, isResponder = false) // Relay
        val nodeC = SimulatedNode(3.toShort(), air, isResponder = true)  // Responder

        // Topology: A <-> B <-> C. A and C cannot hear each other directly!
        air.setBidirectionalLink(nodeA.deviceId, nodeB.deviceId)
        air.setBidirectionalLink(nodeB.deviceId, nodeC.deviceId)

        // 1. A originates SOS with TTL 4
        nodeA.sendSos(ttl = 4, timestamp = 500)
        air.runUntilIdle()

        // Assert 1: Node C alerted exactly once, with TTL 3 on arrival (1 hop via B)
        assertEquals(1, nodeC.alertCount)
        assertNotNull(nodeC.lastReceivedSosPacket)
        assertEquals(3, nodeC.lastReceivedSosPacket?.ttl)
        assertEquals(1.toShort(), nodeC.lastReceivedSosPacket?.senderId)

        // Assert 2: Node B relayed the SOS and relayed the ACK
        assertTrue(nodeB.relayCount >= 2)

        // Assert 3: C's ACK reaches A, and A's responder count is 1 (responder #3)
        assertEquals(1, nodeA.uniqueResponders.size)
        assertTrue(nodeA.uniqueResponders.contains(3.toShort()))
    }

    @Test
    fun testRemoveRelayNodeBPreventsDeliveryToC() {
        val air = SimulatedAir()
        val nodeA = SimulatedNode(1.toShort(), air)
        val nodeB = SimulatedNode(2.toShort(), air)
        val nodeC = SimulatedNode(3.toShort(), air, isResponder = true)

        // B is disconnected from the air
        air.setBidirectionalLink(nodeA.deviceId, nodeB.deviceId)
        // Disconnect link between B and C
        air.removeBidirectionalLink(nodeB.deviceId, nodeC.deviceId)

        // A originates SOS
        nodeA.sendSos(ttl = 4, timestamp = 700)
        air.runUntilIdle()

        // C gets nothing
        assertEquals(0, nodeC.alertCount)
        assertNull(nodeC.lastReceivedSosPacket)
        assertEquals(0, nodeA.uniqueResponders.size)
    }

    @Test
    fun testFourthNodeDHearsEveryoneAndAlertsOnceNotRepeatedly() {
        val air = SimulatedAir()
        val nodeA = SimulatedNode(1.toShort(), air)
        val nodeB = SimulatedNode(2.toShort(), air)
        val nodeC = SimulatedNode(3.toShort(), air)
        val nodeD = SimulatedNode(4.toShort(), air) // In range of all nodes

        air.setBidirectionalLink(nodeA.deviceId, nodeB.deviceId)
        air.setBidirectionalLink(nodeB.deviceId, nodeC.deviceId)

        // D hears A, B, and C
        air.setBidirectionalLink(nodeD.deviceId, nodeA.deviceId)
        air.setBidirectionalLink(nodeD.deviceId, nodeB.deviceId)
        air.setBidirectionalLink(nodeD.deviceId, nodeC.deviceId)

        nodeA.sendSos(ttl = 4, timestamp = 999)
        air.runUntilIdle()

        // D hears original from A (TTL 4), and later duplicate relays from B and C
        // Dedup cache must suppress duplicates: D alerts exactly once!
        assertEquals(1, nodeD.alertCount)
    }

    @Test
    fun testRingTopologyTerminatesWithoutInfiniteLoop() {
        val air = SimulatedAir()
        val nodeA = SimulatedNode(1.toShort(), air)
        val nodeB = SimulatedNode(2.toShort(), air)
        val nodeC = SimulatedNode(3.toShort(), air)
        val nodeD = SimulatedNode(4.toShort(), air)

        // Ring topology: A <-> B <-> C <-> D <-> A
        air.setBidirectionalLink(nodeA.deviceId, nodeB.deviceId)
        air.setBidirectionalLink(nodeB.deviceId, nodeC.deviceId)
        air.setBidirectionalLink(nodeC.deviceId, nodeD.deviceId)
        air.setBidirectionalLink(nodeD.deviceId, nodeA.deviceId)

        nodeA.sendSos(ttl = 4, timestamp = 1234)

        // Run until event queue is empty
        val eventCount = air.runUntilIdle(maxEvents = 1000)

        // Simulation must terminate cleanly without circulating forever
        assertTrue("Simulation must finish with finite events", eventCount < 1000)
        assertEquals(1, nodeB.alertCount)
        assertEquals(1, nodeC.alertCount)
        assertEquals(1, nodeD.alertCount)
    }

    @Test
    fun testRelayBudgetCapEnforcesMaximum12RelaysPerMinute() {
        val air = SimulatedAir()
        val relayNode = SimulatedNode(99.toShort(), air)

        var relaysApproved = 0
        // Send 100 distinct SOS packets within a single simulated 60s window
        for (i in 1..100) {
            val packet = SosPacket(
                senderId = i.toShort(),
                timestamp = i,
                lat = 12.0f,
                lon = 77.0f,
                flags = SosPacket.FLAG_SOS.toByte(),
                ttl = 3
            )
            val decision = RelayPolicy.evaluate(
                packet = packet,
                myDeviceId = relayNode.deviceId,
                cache = relayNode.cache,
                canRelayBudget = { relayNode.relayBudget.tryAcquire() }
            )
            if (decision == RelayDecision.ALERT_AND_RELAY || decision == RelayDecision.RELAY_ONLY) {
                relaysApproved++
            }
        }

        // Exactly 12 relays permitted per minute under battery protection cap
        assertEquals(Constants.MESH_MAX_RELAYS_PER_MIN, relaysApproved)
    }
}
