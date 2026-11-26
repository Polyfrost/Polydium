package org.polyfrost.polydium.client.perf

//? if >= 1.21.8 {
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Field

class OcclusionEyeTest {
    private val saved = mutableListOf<Pair<Field, Any?>>()

    private fun set(owner: Class<*>, name: String, value: Any) {
        val field = owner.getDeclaredField(name).apply { isAccessible = true }
        saved += field to field.get(null)
        field.set(null, value)
    }

    private val isUnchanged = OcclusionCuller::class.java.getDeclaredMethod(
        "isUnchanged", Double::class.java, Double::class.java, Double::class.java
    ).apply { isAccessible = true }
    private val isPyramidCurrent = OcclusionCuller::class.java.getDeclaredMethod(
        "isPyramidCurrent", Double::class.java, Double::class.java, Double::class.java
    ).apply { isAccessible = true }

    @Test
    fun `any eye movement retests against the depth`() {
        set(OcclusionCuller::class.java, "culling", true)
        set(OcclusionCuller::class.java, "sectionsChanged", false)
        set(OcclusionCuller::class.java, "retestPending", false)
        set(OcclusionCuller::class.java, "testedX", 10.0)
        set(OcclusionCuller::class.java, "testedY", 64.0)
        set(OcclusionCuller::class.java, "testedZ", -3.0)
        set(OcclusionCuller::class.java, "testedVersion", 1L)
        set(OcclusionDepth::class.java, "latestVersion", 1L)

        try {
            assertTrue(isUnchanged.invoke(null, 10.0, 64.0, -3.0) as Boolean)
            assertTrue(isPyramidCurrent.invoke(null, 10.0, 64.0, -3.0) as Boolean)
            assertFalse(isUnchanged.invoke(null, 10.2, 64.0, -3.0) as Boolean)
            assertFalse(isPyramidCurrent.invoke(null, 10.0, 64.1, -3.0) as Boolean)
        } finally {
            for ((field, value) in saved.asReversed()) field.set(null, value)
        }
    }
}
//?}
