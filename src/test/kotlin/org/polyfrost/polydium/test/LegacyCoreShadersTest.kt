package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.LegacyCoreShaders

class LegacyCoreShadersTest {
    @Test
    fun `detects pre-26_3 imports only`() {
        assertTrue(LegacyCoreShaders.isLegacy("#version 330\n\n#moj_import <minecraft:fog.glsl>\n"))
        assertFalse(LegacyCoreShaders.isLegacy("#version 330\n#include <minecraft:fog.glsl>\nlayout(location = 0) out vec4 fragColor;\n"))
    }
}
