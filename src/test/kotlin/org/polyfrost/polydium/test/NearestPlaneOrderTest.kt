package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.lwjgl.system.MemoryUtil
import org.polyfrost.polydium.client.perf.NearestPlaneOrder
import java.util.Random

class NearestPlaneOrderTest {
    private val quadSize = NearestPlaneOrder.STRIDE * 4

    private fun quantize(v: Float) = ((8f + v) / 32f * (1 shl 20)).toInt() and 0xFFFFF
    private fun hi(x: Int, y: Int, z: Int) = (x ushr 10 and 0x3FF) or ((y ushr 10 and 0x3FF) shl 10) or ((z ushr 10 and 0x3FF) shl 20)
    private fun lo(x: Int, y: Int, z: Int) = (x and 0x3FF) or ((y and 0x3FF) shl 10) or ((z and 0x3FF) shl 20)

    @Test
    fun `keeps each facing's quads and orders them furthest plane first, stable on ties`() {
        val random = Random(1)
        val planes = floatArrayOf(0f, 1f / 2048f, 0.0625f, 0.5f, 1f, 3.25f, 15f, 16f, -0.5f)
        repeat(500) {
            val facing = random.nextInt(6)
            val axis = facing % 3
            val quads = random.nextInt(60)
            val base = MemoryUtil.nmemAlloc(quads.toLong() * quadSize + 1)
            val keys = IntArray(quads)
            for (quad in 0 until quads) {
                val plane = planes[random.nextInt(planes.size)]
                var min = Int.MAX_VALUE
                for (vertex in 0 until 4) {
                    val c = FloatArray(3) { random.nextFloat() * 16f }
                    c[axis] = plane
                    val q = IntArray(3) { quantize(c[it]) }
                    val address = base + quad.toLong() * quadSize + vertex.toLong() * NearestPlaneOrder.STRIDE
                    MemoryUtil.memPutInt(address, hi(q[0], q[1], q[2]))
                    MemoryUtil.memPutInt(address + 4, lo(q[0], q[1], q[2]))
                    MemoryUtil.memPutInt(address + 8, quad)
                    MemoryUtil.memPutInt(address + 12, random.nextInt())
                    MemoryUtil.memPutInt(address + 16, random.nextInt())
                    min = minOf(min, if (facing < 3) q[axis] else -q[axis])
                }
                keys[quad] = -Math.floorDiv(min + 16, 32)
            }
            val before = (0 until quads).map { bytes(base + it.toLong() * quadSize) }

            NearestPlaneOrder.sort(base, quads, facing)

            val after = (0 until quads).map { bytes(base + it.toLong() * quadSize) }
            val expected = (0 until quads).sortedWith(compareBy({ keys[it] }, { it })).map { before[it] }
            assertEquals(before.sorted(), after.sorted())
            assertEquals(expected, after)
            MemoryUtil.nmemFree(base)
        }
    }

    private fun bytes(address: Long) = (0 until quadSize).joinToString(",") { MemoryUtil.memGetByte(address + it).toString() }
}
