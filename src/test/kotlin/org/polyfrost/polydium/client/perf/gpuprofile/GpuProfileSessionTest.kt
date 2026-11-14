package org.polyfrost.polydium.client.perf.gpuprofile

import it.unimi.dsi.fastutil.longs.LongArrayList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.OptionalLong

class GpuProfileSessionTest {
    private val keyParts = listOf(longArrayOf(0, 1), longArrayOf(2, 3))

    private fun session(passLevel: Boolean) =
        GpuProfileSession(GpuProfiler.Request(1) {}, 0L, mutableMapOf(), passLevel)

    @Test
    fun `discarded samples are skipped and continuations add time but no call`() {
        val session = session(false)
        val keys = intArrayOf(0, GpuProfiler.DISCARDED, 1, 0)
        val draws = intArrayOf(5, 0, 0, GpuProfiler.CONTINUED)
        val stamps = longArrayOf(0, 10, 10, 12, 12, 40, 40, 45)

        session.addFrame(keys, draws, stamps, 4, 0L, false, keyParts)

        assertEquals(15, session.byKey.nanos[0])
        assertEquals(1, session.byKey.calls[0])
        assertEquals(5, session.byKey.draws[0])
        assertEquals(28, session.byKey.nanos[1])
        assertEquals(43, session.measured.getLong(0))
        assertEquals(45, session.gpuSpans.getLong(0))
    }

    @Test
    fun `pass level charges each call the time since the previous one ended`() {
        val session = session(true)
        val keys = intArrayOf(0, 0, GpuProfiler.DISCARDED, 1)
        val draws = intArrayOf(1, 1, 0, 1)
        val stamps = longArrayOf(100, 100, 100, 100, 130, 130, 160, 160)

        session.addFrame(keys, draws, stamps, 4, 0L, false, keyParts)

        assertEquals(0, session.byKey.nanos[0])
        assertEquals(60, session.byKey.nanos[1])
        assertEquals(60, session.measured.getLong(0))
    }

    @Test
    fun `pass level charges the first pass to a begin stamp written before it`() {
        val session = session(true)
        val keys = intArrayOf(0, 1)
        val draws = intArrayOf(0, 1)
        val stamps = longArrayOf(100, 130, 130, 130)

        session.addFrame(keys, draws, stamps, 2, 0L, false, keyParts)

        assertEquals(30, session.byKey.nanos[0])
        assertEquals(0, session.byKey.nanos[1])
        assertEquals(30, session.measured.getLong(0))
    }

    @Test
    fun `every frame is counted once and averages divide by the frame count`() {
        val session = session(false)

        for (frame in 1..3)
            session.addFrame(intArrayOf(0, 0), intArrayOf(2, 1), longArrayOf(0, 10L * frame, 50, 60), 2, 1_000_000L * frame, false, keyParts)

        assertEquals(3, session.frames)
        assertEquals(3, session.measured.size)
        assertEquals(3, session.gpuSpans.size)
        assertEquals(3, session.cpuFrames.size)
        assertEquals(90, session.byKey.nanos[0])
        assertEquals(40, session.byKey.maxFrameNanos[0])
        assertEquals(6, session.byKey.calls[0])
        assertEquals(9, session.byKey.draws[0])
        assertEquals(3, session.byKey.framesSeen[0])
        assertEquals(30.0 / 1e6, GpuProfileReport.avgMs(session.measured), 1e-12)
        assertEquals(2.0, GpuProfileReport.avgMs(session.cpuFrames), 1e-12)
    }

    @Test
    fun `percentiles pick the nearest rank and an empty series reads zero`() {
        val values = LongArrayList()

        for (ms in longArrayOf(70, 3, 100, 50, 1) + (2L..99L).filter { it != 3L && it != 50L && it != 70L })
            values.add(ms * 1_000_000L)

        assertEquals(50.0, GpuProfileReport.percentileMs(values, 0.5), 1e-9)
        assertEquals(99.0, GpuProfileReport.percentileMs(values, 0.99), 1e-9)
        assertEquals(100.0, GpuProfileReport.percentileMs(values, 1.0), 1e-9)
        assertEquals(1.0, GpuProfileReport.percentileMs(values, 0.0), 1e-9)
        assertEquals(0.0, GpuProfileReport.percentileMs(LongArrayList(), 0.5))
        assertEquals(0.0, GpuProfileReport.percentileMs(LongArrayList(), 1.0))
        assertEquals(0.0, GpuProfileReport.avgMs(LongArrayList()))
    }

    @Test
    fun `reads never cross a pool`() {
        assertEquals(96, GpuStamps.chunk(4000, 500, 1024))
        assertEquals(1024, GpuStamps.chunk(4096, 5000, 1024))
        assertEquals(7, GpuStamps.chunk(8192, 7, 1024))
        assertEquals(16, GpuStamps.pools(65536))
    }

    @Test
    fun `tick differences wrap at the queue's valid bits`() {
        val wrap = 1L shl 36

        assertEquals(15L, GpuStamps.ticksSince(wrap - 5, 10, 36))
        assertEquals(-15L, GpuStamps.ticksSince(10, wrap - 5, 36))
        assertEquals(7L, GpuStamps.ticksSince(3, 10, 36))
        assertEquals(15L, GpuStamps.ticksSince(-5, 10, 64))
    }

    private fun pools(ticks: Long, missing: Int = -1) = GpuStamps.PoolReader { pool, query, count ->
        assertTrue(query + count <= GpuStamps.POOL_SIZE && count <= GpuStamps.READ_CHUNK, "read $count from $query")
        Array(count) {
            val index = pool * GpuStamps.POOL_SIZE + query + it
            if (index == missing) OptionalLong.empty() else OptionalLong.of(ticks + index)
        }
    }

    @Test
    fun `pool stamps are read across pools and converted to nanoseconds since the first`() {
        val first = GpuStamps.POOL_SIZE - 10
        val nanos = LongArray(2000)

        assertTrue(GpuStamps.readPools(first, nanos.size, nanos, 64, 2.5, pools(1_000_000L)))

        for (i in nanos.indices)
            assertEquals(Math.round(i * 2.5), nanos[i], "stamp $i")
    }

    @Test
    fun `pool stamps straddling a counter wrap stay increasing`() {
        val nanos = LongArray(8)

        assertTrue(GpuStamps.readPools(0, nanos.size, nanos, 36, 1.0, pools((1L shl 36) - 4)))
        assertEquals((0L until 8L).toList(), nanos.toList())
    }

    @Test
    fun `a pool read with any stamp not yet available fails`() {
        assertFalse(GpuStamps.readPools(0, 3000, LongArray(3000), 64, 1.0, pools(0L, missing = 2500)))
    }
}
