package org.polyfrost.polydium.client.perf

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OitlessRenderTypesTest {
    private class Type(val oit: Boolean)

    @Test
    fun `answers each render type once until fonts reload`() {
        var checks = 0
        val cache = OitlessRenderTypes<Type> { checks++; !it.oit }
        val oit = Type(true)
        val oitless = Type(false)

        repeat(100) {
            assertFalse(cache.lacksOit(oit))
            assertTrue(cache.lacksOit(oitless))
        }
        assertEquals(2, checks)

        FontGeneration.bump()
        assertTrue(cache.lacksOit(oitless))
        assertEquals(3, checks)
        assertEquals(1, cache.size())
    }

    @Test
    fun `keys by identity, not equality`() {
        val cache = OitlessRenderTypes<String> { it.startsWith("x") }
        val a = String(charArrayOf('x'))
        val b = String(charArrayOf('x'))
        cache.lacksOit(a)
        cache.lacksOit(b)
        assertEquals(2, cache.size())
    }
}
