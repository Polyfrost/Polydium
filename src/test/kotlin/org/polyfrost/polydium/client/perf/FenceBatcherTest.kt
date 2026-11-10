package org.polyfrost.polydium.client.perf

//? if >= 1.21.8 && < 26.2 {
/*import com.mojang.blaze3d.buffers.GpuFence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.function.Supplier

class FenceBatcherTest {
    private class FakeFence : GpuFence {
        var closes = 0
        var waits = 0

        override fun close() {
            closes++
        }

        override fun awaitCompletion(timeout: Long): Boolean {
            waits++
            return true
        }
    }

    @Test
    fun `shares one fence per frame and closes it once`() {
        val created = mutableListOf<FakeFence>()
        FenceBatcher.factory = Supplier { FakeFence().also(created::add) }
        FenceBatcher.flush()
        created.clear()

        val a = FenceBatcher.defer()
        val b = FenceBatcher.defer()
        val c = FenceBatcher.defer()
        FenceBatcher.flush()
        assertEquals(1, created.size)

        assertTrue(a.awaitCompletion(Long.MAX_VALUE))
        assertTrue(b.awaitCompletion(Long.MAX_VALUE))
        assertEquals(1, created[0].waits)
        a.close()
        b.close()
        b.close()
        assertEquals(0, created[0].closes)
        c.close()
        assertEquals(1, created[0].closes)

        val early = FenceBatcher.defer()
        early.awaitCompletion(Long.MAX_VALUE)
        assertEquals(2, created.size)
        assertEquals(1, created[1].waits)
        early.close()
        assertEquals(1, created[1].closes)

        val dropped = FenceBatcher.defer()
        dropped.close()
        FenceBatcher.flush()
        assertEquals(2, created.size)
    }
}
*///?}
