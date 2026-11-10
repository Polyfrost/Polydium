package org.polyfrost.polydium.client.perf

//? if >= 26.3 {
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.IdentityHashMap

class IrisOverrideSweepTest {
    private class Base(private val closed: Boolean) : CompiledRenderPipeline {
        override fun isClosed() = closed
        override fun close() = Unit
    }

    private fun overrides(vararg entries: Pair<Base, Map<Any, String>>): MutableMap<Any, MutableMap<Any, MutableMap<Any, Any>>> {
        val outer = IdentityHashMap<Any, MutableMap<Any, MutableMap<Any, Any>>>()
        for ((base, programs) in entries) {
            val byProgram = IdentityHashMap<Any, MutableMap<Any, Any>>()
            programs.forEach { (program, pipeline) -> byProgram[program] = hashMapOf<Any, Any>(listOf<Any>() to pipeline) }
            outer[base] = byProgram
        }
        return outer
    }

    private fun sweep(overrides: Map<Any, *>, vararg destroyed: Any): List<Any> {
        val closed = ArrayList<Any>()
        IrisOverrideSweep.sweep(overrides, destroyed.toSet().toMutableSet(), closed::add)
        return closed
    }

    @Test
    fun `closed base drops overrides of live programs`() {
        val base = Base(closed = true)
        val overrides = overrides(base to mapOf(Any() to "a", Any() to "b"))

        assertEquals(setOf("a", "b"), sweep(overrides).toSet())
        assertEquals(0, overrides.size)
    }

    @Test
    fun `destroyed program drops only its overrides`() {
        val destroyed = Any()
        val kept = Any()
        val base = Base(closed = false)
        val other = Base(closed = false)
        val overrides = overrides(base to mapOf(destroyed to "a", kept to "b"), other to mapOf(destroyed to "c"))

        assertEquals(setOf("a", "c"), sweep(overrides, destroyed).toSet())
        assertEquals(1, overrides.size)
        assertEquals(setOf(kept), overrides[base]!!.keys)
    }

    @Test
    fun `live base and program stay`() {
        val overrides = overrides(Base(closed = false) to mapOf(Any() to "a"))

        assertEquals(emptyList<Any>(), sweep(overrides))
        assertEquals(1, overrides.size)
    }
}
//?}
