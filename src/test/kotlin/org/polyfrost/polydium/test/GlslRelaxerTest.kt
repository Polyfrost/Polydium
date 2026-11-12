package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.vulkan.glsl.GlslRelaxer

class GlslRelaxerTest {
    @Test
    fun `relaxes GL-only patterns`() {
        val out = GlslRelaxer.relax(
            """
            #version 120 compatibility
            #extension GL_NV_gpu_shader5 : require
            uniform sampler2D Sampler0;
            uniform float GameTime;
            uniform vec2 A = vec2(1, 2), B[2];
            layout(location = 3) uniform highp mat4 M;
            layout(shared) uniform Blk { vec4 X; };
            """.trimIndent()
        )

        assertTrue(out.startsWith("#version 150\n"))
        assertTrue(out.contains("#extension GL_NV_gpu_shader5 : enable"))
        assertTrue(out.contains("uniform sampler2D Sampler0;"))
        assertTrue(out.contains("float GameTime = float(0);"))
        assertTrue(out.contains("vec2 A = vec2(1, 2);vec2 B[2] = vec2[2](vec2(0), vec2(0));"))
        assertTrue(out.contains("mat4 M = mat4(0);"))
        assertTrue(out.contains("layout(std140) uniform Blk { vec4 X; };"))
        assertFalse(out.contains("compatibility"))
    }

    @Test
    fun `keeps a single version directive when it has a trailing comment`() {
        val out = GlslRelaxer.relax("#version 450 // core\nout vec4 c;\nvoid main() { c = vec4(1); }\n")

        assertEquals(1, Regex("#version").findAll(out).count())
        assertTrue(out.startsWith("#version 450\n"))
    }
}
