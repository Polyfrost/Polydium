package org.polyfrost.polydium.test

//? if >= 1.21.11 {
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.FramePath

class FramePathTest {
    private var now = 1L

    private fun window(inlineFrameNanos: Long, workerFrameNanos: Long, waitNanos: Long = 0L): Boolean {
        var usedWorker = false
        repeat(128) {
            usedWorker = FramePath.beginFrame(true)
            if (!usedWorker) FramePath.recordGpuWait(waitNanos)
            now += if (usedWorker) workerFrameNanos else inlineFrameNanos
            FramePath.frameSubmitted(now)
        }
        return usedWorker
    }

    private fun start() {
        FramePath.reset()
        FramePath.beginFrame(true)
        FramePath.frameSubmitted(now)
    }

    @Test
    fun `blocked inline path switches to the worker when the worker measures faster`() {
        start()
        repeat(8) { assertFalse(window(6_800_000L, 5_700_000L, 2_000_000L)) }
        assertTrue(window(6_800_000L, 5_700_000L, 2_000_000L))
        repeat(7) { assertTrue(window(6_800_000L, 5_700_000L, 2_000_000L)) }
    }

    @Test
    fun `blocked inline path stays inline when the worker is not faster`() {
        start()
        repeat(8) { assertFalse(window(6_800_000L, 6_800_000L, 2_000_000L)) }
        assertTrue(window(6_800_000L, 6_800_000L, 2_000_000L))
        repeat(7) { assertFalse(window(6_800_000L, 6_800_000L, 2_000_000L)) }
    }

    @Test
    fun `unblocked inline path enables the worker without probing`() {
        start()
        window(5_000_000L, 5_000_000L)
        assertTrue(window(5_000_000L, 5_000_000L))
    }
}
//?}
