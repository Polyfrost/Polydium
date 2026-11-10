package org.polyfrost.polydium.client.perf

//? if >= 1.21.8 {
import org.joml.Matrix4f
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Random

class OcclusionCarveTest {
    private val clearDepth = OcclusionCuller::class.java.getDeclaredMethod(
        "clearDepth", Int::class.java, Int::class.java, Int::class.java, Int::class.java, Boolean::class.java, FloatArray::class.java
    ).apply { isAccessible = true }
    private val excludeOccluders = OcclusionCuller::class.java.getDeclaredMethod("excludeOccluders").apply { isAccessible = true }
    private val terrain = 0.00015f

    private fun boxes(count: Int): IntArray {
        val random = Random(7)

        return IntArray(count * 4).also {
            for (box in 0 until count) {
                val z = -200 - random.nextInt(400)
                it[box * 4] = (z * (0.2 + random.nextDouble() * 0.7)).toInt()
                it[box * 4 + 1] = -8 + random.nextInt(32)
                it[box * 4 + 2] = z
                it[box * 4 + 3] = SectionBounds.FULL
            }
        }
    }

    private fun prepare(boxes: IntArray, depth: Float) {
        val matrix = OcclusionDepth.latestMatrix()
        Matrix4f().perspective(Math.toRadians(70.0).toFloat(), 16f / 9f, 0.05f, 1000f, true).get(matrix)

        for (column in 0 until 16 step 4)
            matrix[column + 2] = matrix[column + 3] - matrix[column + 2]

        OcclusionDepth.latestDepth().fill(depth)
        OcclusionDepth.latestExcluded().clear()
        OcclusionDepth.latestExcluded().addElements(0, boxes)
    }

    private fun exact(boxes: IntArray, depth: Float = terrain): FloatArray {
        prepare(boxes, depth)

        for (box in boxes.indices step 4)
            clearDepth.invoke(null, boxes[box], boxes[box + 1], boxes[box + 2], boxes[box + 3], false, null)

        return OcclusionDepth.latestDepth().copyOf()
    }

    private fun excluded(boxes: IntArray, depth: Float = terrain): FloatArray {
        prepare(boxes, depth)
        excludeOccluders.invoke(null)

        return OcclusionDepth.latestDepth().copyOf()
    }

    @Test
    fun `carves each box exactly up to the limit`() {
        val boxes = boxes(4096)

        assertArrayEquals(exact(boxes), excluded(boxes))
    }

    @Test
    fun `past the limit clears every texel a box covers but keeps the rest`() {
        val boxes = boxes(6000)
        val exact = exact(boxes)
        val merged = excluded(boxes)

        for (index in exact.indices)
            assertTrue(exact[index] != 0f || merged[index] == 0f, "texel $index kept depth a box wrote")

        val kept = merged.count { it == terrain }

        assertTrue(exact.count { it == terrain } < exact.size, "boxes covered nothing")
        assertTrue(kept > merged.size / 2, "only $kept of ${merged.size} texels kept")

        val edges = arrayOf(
            intArrayOf(-16, 128, -192),
            intArrayOf(-16, -144, -192),
            intArrayOf(-240, -16, -192),
            intArrayOf(224, -16, -192),
            intArrayOf(-16, -16, -16)
        )

        for ((edge, point) in edges.withIndex()) {
            val sparse = IntArray(4097 * 4)

            for (box in 0 until 4097) {
                sparse[box * 4] = 1_000_000 + box * 16
                sparse[box * 4 + 2] = -192
                sparse[box * 4 + 3] = SectionBounds.FULL
            }

            for (axis in 0..2)
                sparse[axis] = point[axis]

            val depth = if (edge == 4) 1f else terrain
            val expected = exact(sparse, depth)
            val actual = excluded(sparse, depth)

            assertTrue(expected.any { it == 0f }, "edge $edge covered nothing")

            for (index in expected.indices)
                assertTrue(expected[index] != 0f || actual[index] == 0f, "edge $edge kept texel $index")

            if (edge == 0)
                assertTrue((179 * 320 until 180 * 320).any { expected[it] == 0f }, "last tile row not exercised")
        }
    }
}
//?}
