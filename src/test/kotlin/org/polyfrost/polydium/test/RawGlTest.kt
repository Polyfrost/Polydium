package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.lwjgl.opengl.GL11C
import org.lwjgl.system.Configuration
import org.polyfrost.polydium.client.perf.RawGl

class RawGlTest {
    // A regression here aborts the whole test JVM instead of failing an assertion
    @Test
    fun `raw OpenGL calls without a context return instead of aborting`() {
        // keeps LWJGL from loading a system OpenGL library, which CI doesn't have
        Configuration.OPENGL_EXPLICIT_INIT.set(true)
        RawGl.install("vendor", "renderer", "version", 16384)

        GL11C.glEnable(GL11C.GL_BLEND)
        // mods loop on this until it reports no error
        assertEquals(GL11C.GL_NO_ERROR, GL11C.glGetError())
        assertNull(GL11C.glGetString(GL11C.GL_EXTENSIONS))
        // array overloads take their function from the capabilities rather than the per-thread table
        GL11C.glGetFloatv(GL11C.GL_LINE_WIDTH, FloatArray(1))
    }
}
