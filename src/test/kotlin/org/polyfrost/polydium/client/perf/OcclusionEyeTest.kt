package org.polyfrost.polydium.client.perf

//? if >= 1.21.8 {
import org.joml.Matrix4f
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Field

class OcclusionEyeTest {
    private val saved = mutableListOf<Pair<Field, Any?>>()

    private fun field(name: String): Field = OcclusionCuller::class.java.getDeclaredField(name).apply { isAccessible = true }

    private fun set(owner: Class<*>, name: String, value: Any) {
        val field = owner.getDeclaredField(name).apply { isAccessible = true }
        saved += field to field.get(null)
        field.set(null, value)
    }

    @AfterEach
    fun restore() {
        for ((field, value) in saved.asReversed()) field.set(null, value)
        saved.clear()
    }

    private val matrix = field("TESTED_MATRIX").get(null) as FloatArray
    private val depth = field("TESTED_DEPTH").get(null) as FloatArray
    private val cornerSizes = field("CORNER_SIZES").get(null) as IntArray
    private val buildPyramid = OcclusionCuller::class.java.getDeclaredMethod("buildPyramid").apply { isAccessible = true }
    private val isBoxOccluded = OcclusionCuller::class.java.getDeclaredMethod(
        "isBoxOccluded", Float::class.java, Float::class.java, Float::class.java, Int::class.java
    ).apply { isAccessible = true }
    private val isUnchanged = OcclusionCuller::class.java.getDeclaredMethod(
        "isUnchanged", Double::class.java, Double::class.java, Double::class.java
    ).apply { isAccessible = true }
    private val isPyramidCurrent = OcclusionCuller::class.java.getDeclaredMethod(
        "isPyramidCurrent", Double::class.java, Double::class.java, Double::class.java
    ).apply { isAccessible = true }

    @Test
    fun `small eye movement reuses the tested occlusion`() {
        set(OcclusionCuller::class.java, "culling", true)
        set(OcclusionCuller::class.java, "sectionsChanged", false)
        set(OcclusionCuller::class.java, "retestPending", false)
        set(OcclusionCuller::class.java, "testedX", 10.0)
        set(OcclusionCuller::class.java, "testedY", 64.0)
        set(OcclusionCuller::class.java, "testedZ", -3.0)
        set(OcclusionCuller::class.java, "testedVersion", 1L)
        set(OcclusionDepth::class.java, "latestVersion", 1L)

        assertTrue(isUnchanged.invoke(null, 10.2, 64.0, -3.0) as Boolean)
        assertTrue(isPyramidCurrent.invoke(null, 10.0, 64.1, -3.0) as Boolean)
        assertFalse(isUnchanged.invoke(null, 10.6, 64.0, -3.0) as Boolean)
        assertFalse(isPyramidCurrent.invoke(null, 10.0, 64.0, -2.4) as Boolean)
    }

    private fun setUpView() {
        set(OcclusionCuller::class.java, "testedX", 0.0)
        set(OcclusionCuller::class.java, "testedY", 0.0)
        set(OcclusionCuller::class.java, "testedZ", 0.0)
        set(OcclusionCuller::class.java, "sourceX", 0.0)
        set(OcclusionCuller::class.java, "sourceY", 0.0)
        set(OcclusionCuller::class.java, "sourceZ", 0.0)
        set(OcclusionCuller::class.java, "nearestBuilt", false)
        set(OcclusionCuller::class.java, "eyeSlack", 0.0)

        Matrix4f().perspective(Math.toRadians(70.0).toFloat(), 16f / 9f, 0.05f, 1000f, true).get(matrix)

        for (column in 0 until 16 step 4)
            matrix[column + 2] = matrix[column + 3] - matrix[column + 2]
    }

    private fun wallDepth(distance: Float): Float {
        val z = -distance
        return (matrix[10] * z + matrix[14]) / (matrix[11] * z + matrix[15])
    }

    private fun screenRect(minX: Float, minY: Float, minZ: Float, size: Float): IntArray {
        var left = Float.POSITIVE_INFINITY
        var bottom = Float.POSITIVE_INFINITY
        var right = Float.NEGATIVE_INFINITY
        var top = Float.NEGATIVE_INFINITY

        for (corner in 0 until 8) {
            val x = minX + if (corner and 1 == 0) 0f else size
            val y = minY + if (corner and 2 == 0) 0f else size
            val z = minZ + if (corner and 4 == 0) 0f else size
            val w = matrix[3] * x + matrix[7] * y + matrix[11] * z + matrix[15]
            val screenX = ((matrix[0] * x + matrix[4] * y + matrix[8] * z + matrix[12]) / w * 0.5f + 0.5f) * OcclusionDepth.WIDTH
            val screenY = ((matrix[1] * x + matrix[5] * y + matrix[9] * z + matrix[13]) / w * 0.5f + 0.5f) * OcclusionDepth.HEIGHT

            left = minOf(left, screenX)
            bottom = minOf(bottom, screenY)
            right = maxOf(right, screenX)
            top = maxOf(top, screenY)
        }

        return intArrayOf(left.toInt(), bottom.toInt(), right.toInt(), top.toInt())
    }

    private fun occluded(slack: Double): Boolean {
        field("eyeSlack").set(null, slack)
        cornerSizes.fill(0)

        return isBoxOccluded.invoke(null, -8f, -8f, -38f, SectionBounds.FULL) as Boolean
    }

    @Test
    fun `a moved eye sees past a nearby occluder`() {
        setUpView()

        val rect = screenRect(-8f, -8f, -38f, 16f)
        val pillar = wallDepth(1f)

        depth.fill(0f)
        for (y in rect[1] - 32..rect[3] + 32)
            for (x in rect[0] - 32..rect[2] + 32)
                depth[y * OcclusionDepth.WIDTH + x] = pillar
        buildPyramid.invoke(null)

        assertTrue(occluded(0.0))
        assertFalse(occluded(0.4))
    }

    @Test
    fun `a moved eye may see through an opening hidden behind a nearer post`() {
        setUpView()

        val rect = screenRect(-8f, -8f, -38f, 16f)
        val centerX = (rect[0] + rect[2]) / 2

        depth.fill(wallDepth(15f))
        for (y in 0 until OcclusionDepth.HEIGHT)
            for (x in centerX - 2..centerX + 2)
                depth[y * OcclusionDepth.WIDTH + x] = wallDepth(3f)
        buildPyramid.invoke(null)

        assertTrue(occluded(0.0))
        assertFalse(occluded(0.4))
    }

    @Test
    fun `a distant wall keeps hiding sections from a moved eye`() {
        setUpView()

        depth.fill(wallDepth(15f))
        buildPyramid.invoke(null)

        assertTrue(occluded(0.0))
        assertTrue(occluded(0.4))
    }
}
//?}
