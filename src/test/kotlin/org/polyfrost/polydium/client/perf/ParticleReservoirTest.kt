package org.polyfrost.polydium.client.perf

import com.google.common.collect.EvictingQueue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.ArrayDeque

class ParticleReservoirTest {
    private fun filled(capacity: Int, size: Int) = EvictingQueue.create<Int>(capacity).apply { repeat(size, ::add) }

    @Test
    fun `accepts below the top quarter of the queue`() {
        assertTrue((1..1000).all { ParticleReservoir.accepts(filled(16384, 12288)) })
    }

    @Test
    fun `rejects a full queue`() {
        assertFalse(ParticleReservoir.accepts(filled(16384, 16384)))
    }

    @Test
    fun `thins inside the top quarter`() {
        val queue = filled(16384, 14336)
        val accepted = (1..20000).count { ParticleReservoir.accepts(queue) }
        assertTrue(accepted in 4000..6000, "accepted $accepted")
    }

    @Test
    fun `follows a raised limit`() {
        val queue = filled(65536, 20000)
        assertTrue((1..1000).all { ParticleReservoir.accepts(queue) })
    }

    @Test
    fun `accepts when a mod replaces the queue`() {
        assertTrue(ParticleReservoir.accepts(ArrayDeque(List(20000) { it })))
    }
}
