package org.polyfrost.polydium.test

import net.minecraft.network.chat.Style
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.PerfClock
import org.polyfrost.polydium.client.perf.PreparedTextCache

class PreparedTextCacheTest {
    private fun lookup(cache: PreparedTextCache<String>, id: Int): String? {
        cache.begin(1)
        cache.add(0, Style.EMPTY, id)
        val cached = cache.get(0f, 0f, 0, 0, 0)
        if (cached == null) cache.put(cache.miss(), "v$id")
        return cached
    }

    private fun seen(cache: PreparedTextCache<String>, id: Int): String? {
        cache.begin(1)
        return cache.seen("t$id", 0f, 0f, 0, 0, 0)
    }

    private fun frame(cache: PreparedTextCache<String>, ids: IntRange) {
        PerfClock.frame++
        ids.forEach { lookup(cache, it) }
    }

    @Test
    fun `a frame larger than the cache keeps what is cached`() {
        val cache = PreparedTextCache<String>(4)
        repeat(3) { frame(cache, 0 until 16) }
        PerfClock.frame++
        (0 until 4).forEach { assertEquals("v$it", lookup(cache, it)) }
    }

    @Test
    fun `seen text survives a frame larger than the cache`() {
        val cache = PreparedTextCache<String>(4)
        PerfClock.frame++
        (0 until 16).forEach { seen(cache, it) }
        PerfClock.frame++
        (0 until 16).forEach { id -> if (seen(cache, id) == null) cache.remember("v$id") }
        PerfClock.frame++
        (0 until 4).forEach { assertEquals("v$it", seen(cache, it)) }
    }

    @Test
    fun `text from older frames still ages out`() {
        val cache = PreparedTextCache<String>(4)
        frame(cache, 0 until 4)
        frame(cache, 4 until 8)
        PerfClock.frame++
        assertNull(lookup(cache, 0))
        assertEquals("v7", lookup(cache, 7))
    }
}
