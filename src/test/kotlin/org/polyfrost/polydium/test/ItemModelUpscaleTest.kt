package org.polyfrost.polydium.test

//? if >= 1.21.11 && < 26.1 {
/*import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.renderer.block.model.ItemModelGenerator
import net.minecraft.client.renderer.texture.SpriteContents
import net.minecraft.client.resources.metadata.animation.FrameSize
import net.minecraft.resources.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.UpscaledSprite
import org.polyfrost.polydium.mixin.client.perf.SpriteContentsAccessor

class ItemModelUpscaleTest {
    private val isTransparent = ItemModelGenerator::class.java.getDeclaredMethod(
        "isTransparent", SpriteContents::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java
    ).apply { isAccessible = true }

    private fun SpriteContents.transparent(x: Int, y: Int, width: Int, height: Int) =
        isTransparent.invoke(null, this, 0, x, y, width, height) as Boolean

    private fun upscaledSprite(): SpriteContents {
        val source = NativeImage(8, 8, true)
        source.setPixel(7, 7, -1)
        val contents = SpriteContents(Identifier.withDefaultNamespace("test"), FrameSize(8, 8), source)
        val scaled = NativeImage(16, 16, false)
        for (y in 0 until 16) for (x in 0 until 16) scaled.setPixel(x, y, source.getPixel(x / 2, y / 2))
        val access = contents as SpriteContentsAccessor
        access.`polydium$setWidth`(16)
        access.`polydium$setHeight`(16)
        access.`polydium$setOriginalImage`(scaled)
        access.`polydium$setByMipLevel`(arrayOf(scaled))
        (contents as UpscaledSprite).`polydium$setUpscale`(2)
        source.close()
        return contents
    }

    @Test
    fun `vanilla side faces use unscaled coordinates`() = upscaledSprite().use {
        assertEquals(false, it.transparent(7, 7, 8, 8))
        assertEquals(true, it.transparent(6, 7, 8, 8))
        assertEquals(true, it.transparent(7, 8, 8, 8))
    }

    @Test
    fun `callers passing the upscaled size sample the upscaled image directly`() = upscaledSprite().use {
        assertEquals(false, it.transparent(15, 15, 16, 16))
        assertEquals(false, it.transparent(14, 14, 16, 16))
        assertEquals(true, it.transparent(13, 14, 16, 16))
        assertEquals(true, it.transparent(0, 16, 16, 16))
    }
}
*///?}
