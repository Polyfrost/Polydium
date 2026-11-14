package org.polyfrost.polydium.client.perf.gpuprofile

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GpuProfilerLifecycleTest {
    private class FakeStamps(size: Int) : GpuStamps {
        val written = LongArray(size)
        val writes = IntArray(size)
        private val unrecycled = BooleanArray(size)
        var clock = 0L
        var ready = true
        var closed = false

        override fun write(query: Int, context: Any?) {
            check(!unrecycled[query]) { "query $query written again before it was recycled" }
            unrecycled[query] = true
            writes[query]++
            clock += 10
            written[query] = clock
        }

        override fun recycle(first: Int, count: Int) {
            unrecycled.fill(false, first, first + count)
        }

        override fun read(first: Int, count: Int, nanos: LongArray): Boolean {
            if (!ready)
                return false

            System.arraycopy(written, first, nanos, 0, count)
            return true
        }

        override fun close() {
            closed = true
        }
    }

    private val stamps = FakeStamps(GpuProfiler.SLOTS * GpuProfiler.SAMPLES_PER_FRAME * 2)

    private fun open() {
        assertTrue(GpuProfiler.open(GpuProfiler.Request(3600) {}, 0L, mutableMapOf(), { stamps }))
    }

    @AfterEach
    fun tearDown() {
        GpuProfiler.shutdown()
    }

    @Test
    fun `draws are sampled and read back once their slot comes around`() {
        open()
        GpuProfiler.frame(1L)
        GpuProfiler.beginDraw(null, 3)
        GpuProfiler.end(null)
        GpuProfiler.beginDraw(null, 2)
        GpuProfiler.end(null)

        for (frame in 2L..GpuProfiler.SLOTS + 1L)
            GpuProfiler.frame(frame)

        val session = GpuProfiler.session!!
        val key = GpuProfiler.key(GpuProfiler.NO_PASS, GpuProfiler.UNNAMED)

        assertEquals(GpuProfiler.SLOTS, session.recordedFrames)
        assertTrue(session.frames >= 1)
        assertEquals(2, session.byKey.calls[key])
        assertEquals(5, session.byKey.draws[key])
        assertEquals(20, session.byKey.nanos[key])
    }

    @Test
    fun `a slot is written again only after its stamps were read and recycled`() {
        open()

        for (frame in 1L..GpuProfiler.SLOTS * 3L) {
            GpuProfiler.frame(frame)
            GpuProfiler.beginDraw(null, 1)
            GpuProfiler.end(null)
        }

        val session = GpuProfiler.session!!

        assertEquals(GpuProfiler.SLOTS * 3 - 1, session.recordedFrames)
        assertEquals(GpuProfiler.SLOTS * 2, session.frames)
        assertEquals(0, session.skippedFrames)
        assertEquals(3, stamps.writes[0])
        assertEquals(3, stamps.writes[1])
    }

    @Test
    fun `each frame read back carries the CPU time from its start to the next frame`() {
        val times = longArrayOf(1_000, 1_300, 2_000, 2_100, 5_000, 5_050, 9_000, 9_900, 10_000, 12_000)

        open()

        for (time in times) {
            GpuProfiler.frame(time)
            GpuProfiler.beginDraw(null, 1)
            GpuProfiler.end(null)
        }

        val session = GpuProfiler.session!!
        val expected = (0 until session.frames).map { times[it + 1] - times[it] }

        assertEquals(times.size - GpuProfiler.SLOTS, session.frames)
        assertEquals(expected, session.cpuFrames.toList())
    }

    @Test
    fun `calls past the sample limit are dropped and the frame counted as overflowed`() {
        open()
        GpuProfiler.frame(1L)

        repeat(GpuProfiler.SAMPLES_PER_FRAME + 5) {
            GpuProfiler.beginDraw(null, 1)
            GpuProfiler.end(null)
        }

        for (frame in 2L..GpuProfiler.SLOTS * 2L + 1L) {
            GpuProfiler.frame(frame)

            if (GpuProfiler.recording) {
                GpuProfiler.beginDraw(null, 1)
                GpuProfiler.end(null)
            }
        }

        val session = GpuProfiler.session!!
        val key = GpuProfiler.key(GpuProfiler.NO_PASS, GpuProfiler.UNNAMED)

        assertEquals(GpuProfiler.SLOTS + 1, session.frames)
        assertEquals(1, session.overflowFrames)
        assertEquals((GpuProfiler.SAMPLES_PER_FRAME + GpuProfiler.SLOTS).toLong(), session.byKey.calls[key])
    }

    @Test
    fun `present waits are kept for frames skipped while their slot is pending`() {
        stamps.ready = false
        open()

        for (frame in 1L..6L) {
            GpuProfiler.frame(frame)

            if (GpuProfiler.recording) {
                GpuProfiler.beginDraw(null, 1)
                GpuProfiler.end(null)
            }

            GpuProfiler.presentWait(System.nanoTime() - 5_000_000L, true)
        }

        GpuProfiler.frame(7L)

        val session = GpuProfiler.session!!

        assertEquals(GpuProfiler.SLOTS, session.recordedFrames)
        assertEquals(3, session.skippedFrames)
        assertEquals(6, session.presentWaits.size)

        for (i in 0 until session.presentWaits.size)
            assertTrue(session.presentWaits.getLong(i) >= 5_000_000L)

        assertEquals(session.presentWaits.size, session.presentThreads.values.sum())
    }

    @Test
    fun `a sample split by a deferred flush resumes and counts as one call`() {
        val context = Any()

        open()
        GpuProfiler.frame(1L)
        GpuProfiler.beginDraw(context, 1)
        GpuProfiler.beginFlush(context, GpuProfiler.OP_CLEAR)
        GpuProfiler.endFlush(context)
        GpuProfiler.end(context)

        for (frame in 2L..GpuProfiler.SLOTS + 1L)
            GpuProfiler.frame(frame)

        val session = GpuProfiler.session!!
        val draw = GpuProfiler.key(GpuProfiler.NO_PASS, GpuProfiler.UNNAMED)
        val clear = GpuProfiler.key(GpuProfiler.ENCODER, GpuProfiler.OP_CLEAR)

        assertEquals(1, session.byKey.calls[draw])
        assertEquals(1, session.byKey.draws[draw])
        assertEquals(20, session.byKey.nanos[draw])
        assertEquals(1, session.byKey.calls[clear])
        assertEquals(10, session.byKey.nanos[clear])
    }

    @Test
    fun `a present finished on another thread is charged to the frame that handed it off`() {
        open()
        GpuProfiler.frame(1L)

        val handed = GpuProfiler.presentHandedOff()

        GpuProfiler.frame(2L)

        val session = GpuProfiler.session!!

        assertEquals(0, session.presentWaits.size)

        val worker = Thread({ GpuProfiler.presentWait(System.nanoTime() - 5_000_000L, true, handed) }, "test present worker")

        worker.start()
        worker.join()
        GpuProfiler.frame(3L)

        assertEquals(2, session.presentWaits.size)
        assertTrue(session.presentWaits.getLong(0) >= 5_000_000L)
        assertEquals(0L, session.presentWaits.getLong(1))
        assertEquals(1, session.presentThreads.getInt("test present worker"))
    }

    @Test
    fun `a present still missing after the bucket wraps is counted as incomplete`() {
        open()
        GpuProfiler.frame(1L)

        val handed = GpuProfiler.presentHandedOff()

        for (frame in 2L..GpuProfiler.PRESENT_FRAMES.toLong())
            GpuProfiler.frame(frame)

        val session = GpuProfiler.session!!

        assertEquals(0, session.presentWaits.size)
        GpuProfiler.frame(GpuProfiler.PRESENT_FRAMES + 1L)
        assertEquals(GpuProfiler.PRESENT_FRAMES, session.presentWaits.size)
        assertEquals(1, session.incompletePresentFrames)

        GpuProfiler.presentWait(System.nanoTime() - 5_000_000L, true, handed)
        GpuProfiler.frame(GpuProfiler.PRESENT_FRAMES + 2L)
        assertEquals(0L, session.presentWaits.getLong(0))
        assertEquals(GpuProfiler.PRESENT_FRAMES + 1, session.presentWaits.size)
        assertEquals(1, session.incompletePresentFrames)
    }

    @Test
    fun `a wrap forces only the frame it evicts`() {
        open()
        GpuProfiler.frame(1L)
        GpuProfiler.presentHandedOff()
        GpuProfiler.frame(2L)

        val second = GpuProfiler.presentHandedOff()

        for (frame in 3L..GpuProfiler.PRESENT_FRAMES + 1L)
            GpuProfiler.frame(frame)

        val session = GpuProfiler.session!!

        assertEquals(1, session.presentWaits.size)
        assertEquals(1, session.incompletePresentFrames)

        GpuProfiler.presentWait(System.nanoTime() - 5_000_000L, true, second)
        GpuProfiler.frame(GpuProfiler.PRESENT_FRAMES + 2L)

        assertEquals(1, session.incompletePresentFrames)
        assertTrue(session.presentWaits.getLong(1) > 0L)
    }

    @Test
    fun `present timing does not read the clock while idle`() {
        assertEquals(0L, GpuProfiler.presentStart())
        open()
        assertTrue(GpuProfiler.presentStart() != 0L)
    }

    @Test
    fun `the capture does not finish while a handed off present is outstanding`() {
        assertTrue(GpuProfiler.open(GpuProfiler.Request(1) {}, 0L, mutableMapOf(), { stamps }))
        GpuProfiler.frame(1L)
        GpuProfiler.presentHandedOff()

        for (frame in 0L until GpuProfiler.SLOTS * 2L)
            GpuProfiler.frame(2_000_000_000L + frame)

        assertTrue(GpuProfiler.session != null)
        assertEquals(0, GpuProfiler.session!!.presentWaits.size)
    }

    private fun drainUntilTimeout(handOff: Boolean): GpuProfileSession {
        assertTrue(GpuProfiler.open(GpuProfiler.Request(1) {}, 0L, mutableMapOf(), { stamps }))
        GpuProfiler.frame(1L)
        GpuProfiler.beginDraw(null, 1)
        GpuProfiler.end(null)

        if (handOff)
            GpuProfiler.presentHandedOff()

        val session = GpuProfiler.session!!
        var frame = 2_000_000_000L

        while (GpuProfiler.session != null && frame < 2_000_000_100L)
            GpuProfiler.frame(frame++)

        assertEquals(null, GpuProfiler.session)
        return session
    }

    @Test
    fun `a drain timed out by an outstanding present keeps ready frames and counts the present incomplete`() {
        val session = drainUntilTimeout(true)

        assertEquals(1, session.frames)
        assertEquals(1, session.samples)
        assertEquals(1, session.presentWaits.size)
        assertEquals(1, session.incompletePresentFrames)
        assertEquals(listOf(2_000_000_000L - 1L), session.allCpuFrames.toList())
    }

    @Test
    fun `stamps of a timed-out capture stay open until the next capture opens`() {
        stamps.ready = false

        val session = drainUntilTimeout(false)

        assertEquals(0, session.frames)
        assertTrue(!stamps.closed)

        val next = FakeStamps(stamps.written.size)

        assertTrue(GpuProfiler.open(GpuProfiler.Request(3600) {}, 0L, mutableMapOf(), { next }))
        assertTrue(stamps.closed)
        assertTrue(!next.closed)
    }

    @Test
    fun `passes continued in one render pass instance are joined on pass-level GPUs`() {
        val environment = mutableMapOf("driver" to "MoltenVK 1.3", "gpu" to "Apple M2")

        assertTrue(GpuProfiler.open(GpuProfiler.Request(3600) {}, 0L, environment, { stamps }))
        GpuProfiler.frame(1L)
        GpuProfiler.pass("first")
        GpuProfiler.beginPass(null)
        GpuProfiler.end(null)
        GpuProfiler.beginDraw(null, 1)
        GpuProfiler.end(null)
        GpuProfiler.pass("continued")
        GpuProfiler.passContinued()
        GpuProfiler.beginDraw(null, 2)
        GpuProfiler.end(null)
        GpuProfiler.pass("alone")
        GpuProfiler.beginPass(null)
        GpuProfiler.end(null)

        for (frame in 2L..GpuProfiler.SLOTS + 1L)
            GpuProfiler.frame(frame)

        val session = GpuProfiler.session!!
        val names = listOf("first", "continued", "alone")
        val labels = MutableList(names.maxOf { GpuProfiler.label(it) } + 1) { "" }

        names.forEach { labels[GpuProfiler.label(it)] = it }

        val rows = GpuProfileReport.groupedPasses(session, session.frames, 0.0, labels)

        assertEquals(listOf("first + continued", "alone"), rows.map { it.pass() })
        assertEquals(50e-6 / session.frames, rows[0].avgMs(), 1e-12)
        assertEquals(3.0, rows[0].callsPerFrame() * session.frames, 1e-9)
        assertEquals(3.0, rows[0].drawsPerFrame() * session.frames, 1e-9)
        assertTrue(rows[0].maxMs().isNaN())
        assertEquals(1.0, rows[1].callsPerFrame() * session.frames, 1e-9)
    }

    @Test
    fun `shutdown closes the stamps of a running capture`() {
        open()
        GpuProfiler.frame(1L)
        GpuProfiler.shutdown()

        assertTrue(stamps.closed)
        assertEquals(null, GpuProfiler.session)
    }

    @Test
    fun `a capture that read back no frame is not reported as zero timestamps`() {
        val session = GpuProfileSession(GpuProfiler.Request(1) {}, 0L, mutableMapOf(), false)

        session.recordedFrames = 2

        val summary = GpuProfileReport.write(session, emptyList(), emptyList())

        assertTrue(summary.contains("no frame's GPU timestamps became available"), summary)
    }

    @Test
    fun `frames read back without a timed call are reported as an empty capture`() {
        val session = GpuProfileSession(GpuProfiler.Request(1) {}, 0L, mutableMapOf(), false)

        session.addFrame(IntArray(0), IntArray(0), LongArray(0), 0, 0L, false, emptyList())

        val summary = GpuProfileReport.write(session, emptyList(), emptyList())

        assertTrue(summary.contains("no GPU call was timed"), summary)
    }

    @Test
    fun `renaming a pass keeps the pipeline set before it`() {
        val pipeline = Any()

        open()
        GpuProfiler.frame(1L)
        GpuProfiler.pass("iris pass")
        GpuProfiler.pipeline(pipeline) { "iris:composite" }
        GpuProfiler.renamePass("Iris composite1")
        GpuProfiler.beginDraw(null, 1)
        GpuProfiler.end(null)

        for (frame in 2L..GpuProfiler.SLOTS + 1L)
            GpuProfiler.frame(frame)

        val key = GpuProfiler.key(GpuProfiler.label("Iris composite1"), GpuProfiler.label("iris:composite"))

        assertEquals(1, GpuProfiler.session!!.byKey.calls[key])
    }

    @Test
    fun `legacy shaders are named by the bound program and forgotten after a capture`() {
        val named = Any()

        GpuProfiler.shaderName(named, "shader minecraft:core/position")

        assertEquals("shader minecraft:core/position", GpuProfiler.shaderName(named))
        assertEquals("(unnamed shader)", GpuProfiler.shaderName(Any()))
        assertEquals("(no shader)", GpuProfiler.shaderName(null))

        GpuProfiler.shutdown()

        assertEquals("(unnamed shader)", GpuProfiler.shaderName(named))
    }

    @Test
    fun `pass-level begin-pass rows are charged to no mod`() {
        val totals = GpuProfileSession.Totals()

        totals.add(0, 1_000_000L, 0)
        totals.add(1, 1_000_000L, 1)
        totals.endFrame()

        val labels = listOf("Main", GpuProfiler.PASS_BEGIN_NAME, "sodium:terrain")
        val parts = listOf(longArrayOf(0, 1), longArrayOf(0, 2))

        fun namespaces(passLevel: Boolean) = GpuProfileReport.rows(totals, 1, 2.0, { labels[parts[it][0].toInt()] },
            { labels[parts[it][1].toInt()] }, passLevel).map { it.namespace() }.toSet()

        assertEquals(setOf(GpuProfileReport.UNATTRIBUTED, "sodium"), namespaces(true))
        assertEquals(setOf("minecraft", "sodium"), namespaces(false))
    }

    @Test
    fun `Iris draws and dispatches are charged to the iris namespace`() {
        assertEquals("iris", GpuProfileReport.namespace("Iris composite1", "iris:composite"))
        assertEquals("iris", GpuProfileReport.namespace("(no render pass)", "iris:compute"))
    }

    @Test
    fun `command encoder work outside a pass is charged to the command encoder`() {
        assertEquals("command encoder", GpuProfileReport.namespace("command encoder", "Upload"))
        assertEquals("minecraft", GpuProfileReport.namespace("Main", "Upload"))
    }

    @Test
    fun `legacy shader labels are charged to the shader's namespace`() {
        assertEquals("example", GpuProfileReport.namespace("Main", "shader example:custom"))
        assertEquals("minecraft", GpuProfileReport.namespace("Main", "shader rendertype_solid"))
    }
}
