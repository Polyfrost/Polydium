package org.polyfrost.polydium.client.perf

//? if >= 1.21.11 {
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FramePathTest {
    private var now = 1L

    @BeforeEach
    fun reset() {
        FramePath.reset()
        now = 1L
    }

    private fun run(frames: Int, workerNanos: Long, inlineNanos: Long, workerBlocked: Double): Int {
        var workerFrames = 0
        repeat(frames) {
            val worker = FramePath.beginFrame(true)
            val frame = if (worker) workerNanos else inlineNanos
            if (worker) {
                workerFrames++
                FramePath.recordGpuWait((frame * workerBlocked).toLong())
            } else {
                FramePath.recordGpuWait(frame)
            }
            now += frame
            FramePath.frameSubmitted(now)
        }
        return workerFrames
    }

    @Test
    fun `keeps the worker when it is faster even if it reports blocking`() {
        val workerFrames = run(128 * 200, 330_000L, 450_000L, 0.5)
        assertTrue(workerFrames > 128 * 200 * 0.85, "worker frames $workerFrames")
    }

    @Test
    fun `stays inline when the worker is slower`() {
        val workerFrames = run(128 * 200, 500_000L, 450_000L, 0.5)
        assertTrue(workerFrames < 128 * 200 * 0.15, "worker frames $workerFrames")
    }

    @Test
    fun `retries a much slower worker less often over time`() {
        val workerFrames = run(128 * 200, 30_000_000L, 5_000_000L, 0.0)
        assertTrue(workerFrames <= 128 * 5, "worker frames $workerFrames")
    }

    @Test
    fun `keeps an unblocked worker without probing inline`() {
        val workerFrames = run(128 * 200, 330_000L, 450_000L, 0.0)
        assertTrue(workerFrames >= 128 * 198, "worker frames $workerFrames")
    }

    @Test
    fun `never uses the worker when the frame is not eligible`() {
        repeat(128 * 40) {
            assertTrue(!FramePath.beginFrame(false))
            now += 300_000L
            FramePath.frameSubmitted(now)
        }
    }
}
//?}
