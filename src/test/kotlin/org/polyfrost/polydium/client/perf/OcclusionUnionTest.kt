package org.polyfrost.polydium.client.perf

//? if >= 1.21.8 {
import org.joml.Matrix4f
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Random

class OcclusionUnionTest {
    private fun field(name: String): Any = OcclusionCuller::class.java.getDeclaredField(name).apply { isAccessible = true }.get(null)

    private val matrix = field("TESTED_MATRIX") as FloatArray
    private val depth = field("TESTED_DEPTH") as FloatArray
    private val cornerSizes = field("CORNER_SIZES") as IntArray
    private val buildPyramid = OcclusionCuller::class.java.getDeclaredMethod("buildPyramid").apply { isAccessible = true }
    private val isBoxOccluded = OcclusionCuller::class.java.getDeclaredMethod(
        "isBoxOccluded", Float::class.java, Float::class.java, Float::class.java, Int::class.java
    ).apply { isAccessible = true }
    private val isAabbOccluded = OcclusionCuller::class.java.getDeclaredMethod(
        "isAabbOccluded", Float::class.java, Float::class.java, Float::class.java, Float::class.java, Float::class.java, Float::class.java
    ).apply { isAccessible = true }

    private fun wallDepth(distance: Float): Float {
        val z = -distance
        return (matrix[10] * z + matrix[14]) / (matrix[11] * z + matrix[15])
    }

    @Test
    fun `a hidden union only hides sections that are hidden on their own`() {
        Matrix4f().perspective(Math.toRadians(70.0).toFloat(), 16f / 9f, 0.05f, 1000f, true).get(matrix)

        for (column in 0 until 16 step 4)
            matrix[column + 2] = matrix[column + 3] - matrix[column + 2]

        cornerSizes.fill(0)

        val random = Random(11)
        val wall = wallDepth(60f)

        depth.fill(wall)
        for (hole in 0 until 6) {
            val x = random.nextInt(OcclusionDepth.WIDTH - 20)
            val y = random.nextInt(OcclusionDepth.HEIGHT - 20)
            for (row in y until y + 4 + random.nextInt(16))
                for (column in x until x + 4 + random.nextInt(16))
                    depth[row * OcclusionDepth.WIDTH + column] = 0f
        }
        buildPyramid.invoke(null)

        var hiddenUnions = 0
        var visibleUnions = 0

        repeat(4000) {
            val baseX = (random.nextInt(40) - 20) * 16
            val baseY = (random.nextInt(8) - 4) * 16
            val baseZ = -(80 + random.nextInt(240)) / 16 * 16
            val spanX = 1 + random.nextInt(4)
            val spanY = 1 + random.nextInt(2)
            val spanZ = 1 + random.nextInt(4)
            val union = isAabbOccluded.invoke(
                null, baseX.toFloat(), baseY.toFloat(), baseZ.toFloat(),
                (baseX + spanX * 16).toFloat(), (baseY + spanY * 16).toFloat(), (baseZ + spanZ * 16).toFloat(),
            ) as Boolean

            if (union) hiddenUnions++ else visibleUnions++

            if (!union) return@repeat

            for (sx in 0 until spanX) for (sy in 0 until spanY) for (sz in 0 until spanZ) {
                val section = isBoxOccluded.invoke(
                    null, (baseX + sx * 16).toFloat(), (baseY + sy * 16).toFloat(), (baseZ + sz * 16).toFloat(), SectionBounds.FULL
                ) as Boolean
                assertTrue(section, "section $sx,$sy,$sz of hidden union at $baseX,$baseY,$baseZ is visible on its own")
            }
        }

        assertTrue(hiddenUnions > 100, "only $hiddenUnions unions hidden")
        assertTrue(visibleUnions > 100, "only $visibleUnions unions visible")
    }
}
//?}
