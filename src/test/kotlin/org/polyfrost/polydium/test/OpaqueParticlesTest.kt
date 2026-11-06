package org.polyfrost.polydium.test

//? if < 26.1 {
/*import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.SharedConstants
import net.minecraft.client.renderer.texture.SpriteContents
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.metadata.animation.FrameSize
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.OpaqueParticles
//? if >= 1.21.10 {
import net.minecraft.client.particle.SingleQuadParticle
//?} else {
/^import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.BlockMarker
import net.minecraft.client.particle.BreakingItemParticle
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.TerrainParticle
import net.minecraft.world.level.block.state.BlockState
^///?}
//? if >= 1.21.11 {
import net.minecraft.resources.Identifier
//?} else {
/^import net.minecraft.resources.ResourceLocation
^///?}
//? if < 1.21.10
/^import net.minecraft.server.packs.resources.ResourceMetadata^/

class OpaqueParticlesTest {
    //? if < 1.21.10 {
    /^private abstract class FadingTerrainParticle(level: ClientLevel, state: BlockState) : TerrainParticle(level, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, state)

    ^///?}
    private fun <T> withOpaqueSprite(test: (TextureAtlasSprite) -> T): T {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        val image = NativeImage(1, 1, false)
        //? if >= 1.21.4 {
        image.setPixel(0, 0, -1)
        //?} else {
        /^image.setPixelRGBA(0, 0, -1)
        ^///?}
        //? if >= 1.21.11 {
        val id = Identifier.withDefaultNamespace("test")
        //?} else {
        /^val id = ResourceLocation.withDefaultNamespace("test")
        ^///?}
        //? if >= 1.21.10 {
        val contents = SpriteContents(id, FrameSize(1, 1), image)
        //?} else {
        /^val contents = SpriteContents(id, FrameSize(1, 1), image, ResourceMetadata.EMPTY)
        ^///?}
        return contents.use {
            val constructor = TextureAtlasSprite::class.java.declaredConstructors.single().apply { isAccessible = true }
            //? if >= 1.21.11 {
            test(constructor.newInstance(id, it, 16, 16, 0, 0, 0) as TextureAtlasSprite)
            //?} else {
            /^test(constructor.newInstance(id, it, 16, 16, 0, 0) as TextureAtlasSprite)
            ^///?}
        }
    }

    //? if >= 1.21.10 {
    @Test
    fun `particle stays on the translucent layer once it fades`() = withOpaqueSprite { sprite ->
        assertSame(OpaqueParticles.TERRAIN, OpaqueParticles.layer(SingleQuadParticle.Layer.TERRAIN, sprite, 1.0F))
        assertSame(SingleQuadParticle.Layer.TERRAIN, OpaqueParticles.layer(SingleQuadParticle.Layer.TERRAIN, sprite, 0.5F))
    }
    //?} else {
    /^@Test
    fun `only exact vanilla classes join the opaque group`() = withOpaqueSprite { sprite ->
        val sheet = ParticleRenderType.TERRAIN_SHEET
        for (particle in listOf(TerrainParticle::class.java, BreakingItemParticle::class.java, BlockMarker::class.java)) {
            assertSame(OpaqueParticles.TERRAIN_SHEET, OpaqueParticles.renderType(sheet, sprite, 1.0F, particle))
        }
        assertSame(sheet, OpaqueParticles.renderType(sheet, sprite, 1.0F, FadingTerrainParticle::class.java))
        assertSame(sheet, OpaqueParticles.renderType(sheet, sprite, 0.5F, TerrainParticle::class.java))
    }
    ^///?}
}
*///?}
