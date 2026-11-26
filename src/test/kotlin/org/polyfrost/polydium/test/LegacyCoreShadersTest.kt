package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.LegacyCoreShaders

class LegacyCoreShadersTest {
    @Test
    fun `detects pre-26_3 imports`() {
        assertTrue(LegacyCoreShaders.isLegacy("shaders/core/rendertype_text.fsh", "#version 330\n\n#moj_import <minecraft:fog.glsl>\nlayout(location = 0) out vec4 fragColor;\n"))
        assertTrue(LegacyCoreShaders.isLegacy("shaders/include/fog.glsl", "#moj_import <minecraft:globals.glsl>\n"))
    }

    @Test
    fun `detects pre-26_3 core shaders without location qualifiers`() {
        assertTrue(LegacyCoreShaders.isLegacy("shaders/core/gui.vsh", "#version 330\nin vec3 Position;\nout vec4 vertexColor;\n"))
        assertFalse(LegacyCoreShaders.isLegacy("shaders/core/gui.vsh", "#version 330\n#include <minecraft:projection.glsl>\nlayout(location = 0) in vec3 Position;\n"))
        assertFalse(LegacyCoreShaders.isLegacy("shaders/core/gui.fsh", "#version 330\nlayout (location=0) out vec4 fragColor;\n"))
    }

    @Test
    fun `detects pre-26_3 includes with a version directive`() {
        assertTrue(LegacyCoreShaders.isLegacy("shaders/include/dynamictransforms.glsl", "#version 330\n\nlayout(std140) uniform DynamicTransforms {\n};\n"))
        assertFalse(LegacyCoreShaders.isLegacy("shaders/include/dynamictransforms.glsl", "layout(std140) uniform DynamicTransforms {\n};\n"))
    }

    @Test
    fun `ignores non-shader files`() {
        assertFalse(LegacyCoreShaders.isLegacy("shaders/post/blur.json", "{}"))
    }
}
