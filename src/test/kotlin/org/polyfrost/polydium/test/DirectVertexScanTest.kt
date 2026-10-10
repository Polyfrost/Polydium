package org.polyfrost.polydium.test

//? if >= 1.21.11 {
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.DirectVertexBuffers

class DirectVertexScanTest {
    @Test
    fun `added members do not disable direct writes`() {
        assertTrue(DirectVertexBuffers.addsOnly("etf\$getProvider"))
        assertTrue(DirectVertexBuffers.addsOnly("lambda\$etf\$initETFVertexConsumer\$0"))
    }

    @Test
    fun `reviewed third party mixins do not disable direct writes`() {
        val handler = String::class.java.getMethod("length")
        assertTrue(DirectVertexBuffers.harmless("xaeroplus.mixin.client.mc.MixinBufferBuilder", handler))
        assertTrue(DirectVertexBuffers.harmless("fi.dy.masa.litematica.mixin.render.MixinBufferBuilder", handler))
    }

    @Test
    fun `handlers and possible overwrites do`() {
        assertFalse(DirectVertexBuffers.addsOnly("handler\$cag000\$entityculling\$tryExtractRenderState"))
        assertFalse(DirectVertexBuffers.addsOnly("wrapOperation\$gme000\$polyplus\$writeGlyphIntoTarget"))
        assertFalse(DirectVertexBuffers.addsOnly("addVertex"))
    }
}
//?}
