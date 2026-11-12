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
    fun `keeps a single version directive when comments follow it`() {
        listOf(
            "#version 450 // core\n",
            "#version 450 /* core */ // pack shader\n",
            "#version 450 /* multi\n  line */\n",
            "/* header\n */ #version 450 /* a */ /* b */\n",
        ).forEach { header ->
            val out = GlslRelaxer.relax(header + "out vec4 c;\nvoid main() { c = vec4(1); }\n")

            assertEquals(1, Regex("#version").findAll(out).count(), header)
            assertTrue(out.trimStart().startsWith("#version 450\n"), header)
        }
    }

    @Test
    fun `leaves preprocessor directives and shader-defined aliases alone`() {
        val out = GlslRelaxer.relax(
            """
            #version 330
            #define texture2D(s, c) texture(s, c)
            #define UBO uniform
            vec4 color;
            #define packed 3
            layout(std140) uniform Blk { vec4 X; };
            vec4 shadow2D(sampler2DShadow s, vec3 c) { return vec4(texture(s, c)); }
            """.trimIndent()
        )

        assertFalse(out.contains("#define texture2D texture"))
        assertFalse(out.contains("#define shadow2D("))
        assertTrue(out.contains("#define texture2DLod textureLod"))
        assertTrue(out.contains("#define UBO uniform\nvec4 color;"))
        assertTrue(out.contains("#define packed 3\nlayout(std140) uniform Blk"))
    }
}
