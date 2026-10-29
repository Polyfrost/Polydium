package org.polyfrost.polydium.test

import com.mojang.blaze3d.buffers.Std140Builder
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lwjgl.system.MemoryUtil
import org.polyfrost.polydium.client.perf.TransformWrites
import java.util.Random

class TransformWritesTest {
    private val specials = floatArrayOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, -0.0f, 0.0f, Float.MIN_VALUE, Float.MAX_VALUE)

    private fun Random.float() = if (nextInt(8) == 0) specials[nextInt(specials.size)] else nextFloat() * 2000f - 1000f

    @Test
    fun `direct write matches the std140 builder`() {
        val random = Random(1)
        val expected = MemoryUtil.memAlloc(256)
        val actual = MemoryUtil.memAlloc(256)
        try {
            repeat(200_000) {
                val modelView = Matrix4f().set(FloatArray(16) { random.float() })
                val texture = Matrix4f().set(FloatArray(16) { random.float() })
                val color = Vector4f(random.float(), random.float(), random.float(), random.float())
                val offset = Vector3f(random.float(), random.float(), random.float())
                val start = random.nextInt(4) * 16
                for (buffer in arrayOf(expected, actual)) {
                    buffer.clear()
                    for (i in 0 until 256) buffer.put(i, 0x5A)
                    buffer.position(start)
                }
                Std140Builder.intoBuffer(expected).putMat4f(modelView).putMat4f(texture).putVec4(color).putVec3(offset)
                assertTrue(TransformWrites.write(actual, modelView, color, offset, texture))
                assertEquals(expected.position(), actual.position())
                for (i in 0 until 256) assertEquals(expected.get(i), actual.get(i)) { "byte $i" }
            }
        } finally {
            MemoryUtil.memFree(expected)
            MemoryUtil.memFree(actual)
        }
    }
}
