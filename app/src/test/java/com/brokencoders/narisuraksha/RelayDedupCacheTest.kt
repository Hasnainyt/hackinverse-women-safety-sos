package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.ble.DedupKey
import com.brokencoders.narisuraksha.ble.RelayDedupCache
import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.core.SosPacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class RelayDedupCacheTest {

    @Test
    fun testDuplicateRejected() {
        var simulatedTime = 1000L
        val cache = RelayDedupCache(clock = { simulatedTime })
        val packet = SosPacket.create(senderId = 101.toShort(), timestampSeconds = 500)

        // First insert -> true (new)
        assertTrue(cache.checkAndInsert(packet))
        assertEquals(1, cache.size())

        // Duplicate insert within window -> false
        simulatedTime += 5000L // 5 seconds later
        assertFalse("Duplicate packet must return false", cache.checkAndInsert(packet))
        assertEquals(1, cache.size())
    }

    @Test
    fun testExpiryAfter60Seconds() {
        var simulatedTime = 10_000L
        val cache = RelayDedupCache(clock = { simulatedTime })
        val packet = SosPacket.create(senderId = 101.toShort(), timestampSeconds = 500)

        assertTrue(cache.checkAndInsert(packet))

        // Advance simulated time past 60s
        simulatedTime += Constants.MESH_DEDUP_TTL_MS + 1000L
        assertTrue("Expired packet must be accepted again as new", cache.checkAndInsert(packet))
    }

    @Test
    fun testBoundEnforced() {
        var simulatedTime = 1000L
        val capacity = 5
        val cache = RelayDedupCache(maxCapacity = capacity, clock = { simulatedTime })

        for (i in 1..capacity) {
            simulatedTime += 100L
            val p = SosPacket.create(senderId = i.toShort(), timestampSeconds = i * 10)
            assertTrue(cache.checkAndInsert(p))
        }
        assertEquals(capacity, cache.size())

        // Insert one more beyond capacity
        simulatedTime += 100L
        val pExtra = SosPacket.create(senderId = 99.toShort(), timestampSeconds = 990)
        assertTrue(cache.checkAndInsert(pExtra))
        assertEquals(capacity, cache.size()) // Must stay bounded

        // The oldest entry (senderId = 1) was evicted
        val oldestKey = DedupKey(1.toShort(), 10, SosPacket.FLAG_SOS.toByte())
        assertFalse("Oldest entry must have been evicted", cache.contains(oldestKey))
    }

    @Test
    fun testConcurrentInsertsDoNotCorruptState() {
        val cache = RelayDedupCache(maxCapacity = 500)
        val numThreads = 10
        val insertsPerThread = 50
        val executor = Executors.newFixedThreadPool(numThreads)
        val latch = CountDownLatch(numThreads)
        val insertedCount = AtomicInteger(0)

        for (t in 0 until numThreads) {
            executor.submit {
                try {
                    for (i in 0 until insertsPerThread) {
                        val sender = (t * 1000 + i).toShort()
                        val p = SosPacket.create(senderId = sender, timestampSeconds = i)
                        if (cache.checkAndInsert(p)) {
                            insertedCount.incrementAndGet()
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        assertEquals(numThreads * insertsPerThread, insertedCount.get())
        assertEquals(numThreads * insertsPerThread, cache.size())
    }
}
