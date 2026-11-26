package org.polyfrost.polydium.client.perf

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OitlessTextCacheTest {
    @Test
    fun `scans each text once per variant until fonts reload`() {
        var scans = 0
        val cache = OitlessTextCache<String>(16)
        val text = String(charArrayOf('a'))

        repeat(100) {
            assertTrue(cache.lacksOit(text, 0) { scans++; true })
            assertFalse(cache.lacksOit(text, 3) { scans++; false })
        }
        assertEquals(2, scans)

        FontGeneration.bump()
        assertFalse(cache.lacksOit(text, 0) { scans++; false })
        assertEquals(3, scans)
    }

    @Test
    fun `keys by identity and stays bounded`() {
        val cache = OitlessTextCache<String>(4)
        val a = String(charArrayOf('x'))
        val b = String(charArrayOf('x'))
        assertTrue(cache.lacksOit(a, 0) { true })
        assertFalse(cache.lacksOit(b, 0) { false })
        assertEquals(2, cache.size())

        repeat(10) { cache.lacksOit(String(charArrayOf('y')), 0) { false } }
        assertTrue(cache.size() <= 4)
    }
}
