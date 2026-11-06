package org.polyfrost.polydium.test

//? if < 1.21.11 {
/*import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.renderer.texture.SpriteContents
import net.minecraft.client.resources.metadata.animation.FrameSize
import net.minecraft.resources.ResourceLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.SpriteTransparency
//? if < 1.21.10
import net.minecraft.server.packs.resources.ResourceMetadata

class SpriteMipTransparencyTest {
    private fun translucent(firstPixel: Int, mips: Int): Boolean {
        val image = NativeImage(2, 2, false)
        for (y in 0..1) for (x in 0..1) {
            val color = if (x == 0 && y == 0) firstPixel else -1
            //? if >= 1.21.4 {
            image.setPixel(x, y, color)
            //?} else {
            /^image.setPixelRGBA(x, y, color)
            ^///?}
        }
        val id = ResourceLocation.withDefaultNamespace("test")
        //? if < 1.21.10 {
        val contents = SpriteContents(id, FrameSize(2, 2), image, ResourceMetadata.EMPTY)
        //?} else {
        /^val contents = SpriteContents(id, FrameSize(2, 2), image)
        ^///?}
        return contents.use {
            it.increaseMipLevel(mips)
            (it as SpriteTransparency.Holder).`polydium$hasTranslucent`()
        }
    }

    @Test
    fun `mip alpha blended from clear and solid pixels counts as translucent`() {
        assertEquals(false, translucent(0, mips = 0))
        assertEquals(true, translucent(0, mips = 1))
        assertEquals(false, translucent(-1, mips = 1))
    }
}
*///?}
