package org.polyfrost.polydium.test

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.BufferSetPool

class BufferSetPoolTest {
    @Test
    fun `released sets come back empty`() {
        val pool = BufferSetPool()
        @Suppress("UNCHECKED_CAST")
        val set = pool.apply("type") as ReferenceLinkedOpenHashSet<Any>
        set.add(Any())
        set.add(Any())
        pool.release(set)
        assertEquals(1, pool.pooled())
        val reused = pool.take()
        assertSame(set, reused)
        assertTrue(reused.isEmpty())
        assertEquals(0, pool.pooled())
    }

    @Test
    fun `a set is never pooled twice`() {
        val pool = BufferSetPool()
        val set = pool.take()
        pool.release(set)
        pool.release(set)
        assertEquals(1, pool.pooled())
        assertSame(set, pool.take())
        assertNotSame(set, pool.take())
    }

    @Test
    fun `foreign values and nulls are ignored`() {
        val pool = BufferSetPool()
        pool.release(null)
        pool.release(ObjectLinkedOpenHashSet<Any>())
        pool.release("not a set")
        assertEquals(0, pool.pooled())
    }

    @Test
    fun `pool size is capped`() {
        val pool = BufferSetPool()
        repeat(BufferSetPool.MAX_POOLED + 10) { pool.release(ReferenceLinkedOpenHashSet<Any>()) }
        assertEquals(BufferSetPool.MAX_POOLED, pool.pooled())
    }
}
