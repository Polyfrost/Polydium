package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.SodiumCullTreePool

class SodiumCullTreePoolTest {
    @Test
    fun `released bitmaps come back cleared`() {
        val first = ArrayList<LongArray>()
        SodiumCullTreePool.begin(first)
        assertTrue(SodiumCullTreePool.collecting())
        val array = SodiumCullTreePool.take()
        array[7] = -1L
        SodiumCullTreePool.end()
        assertFalse(SodiumCullTreePool.collecting())
        assertSame(array, first.single())

        SodiumCullTreePool.release(first)
        assertTrue(first.isEmpty())

        val second = ArrayList<LongArray>()
        SodiumCullTreePool.begin(second)
        val reused = SodiumCullTreePool.take()
        SodiumCullTreePool.end()
        assertSame(array, reused)
        assertEquals(0L, reused[7])
        assertSame(reused, second.single())
    }
}
