package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.QuadSort

class QuadSortCacheTest {
    @Test
    fun `keeps the previous frame's used sorts and drops the rest`() {
        val keys = intArrayOf(-1, 0, 1)
        val order = intArrayOf(0, 1, 2)

        QuadSort.startFrame(100)
        assertNull(QuadSort.reuseShared(100, keys, keys.size))
        QuadSort.rememberOwned(order)
        assertNull(QuadSort.reuseShared(100, keys, keys.size))
        QuadSort.rememberOwned(order.clone())

        QuadSort.startFrame(101)
        assertSame(order, QuadSort.reuseShared(101, keys, keys.size))

        QuadSort.startFrame(102)
        assertSame(order, QuadSort.reuseShared(102, keys, keys.size))
        assertNull(QuadSort.reuseShared(102, keys, keys.size))

        QuadSort.startFrame(103)
        QuadSort.startFrame(104)
        assertNull(QuadSort.reuseShared(104, keys, keys.size))
    }
}
