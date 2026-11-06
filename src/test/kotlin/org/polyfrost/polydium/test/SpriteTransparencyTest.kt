package org.polyfrost.polydium.test

//? if >= 1.21.11 && < 26.1 {
/*import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.SharedConstants
import net.minecraft.client.color.item.ItemTintSource
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.item.BlockModelWrapper
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.client.renderer.item.ModelRenderProperties
import net.minecraft.client.renderer.texture.SpriteContents
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection
import net.minecraft.client.resources.metadata.animation.FrameSize
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.server.Bootstrap
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.joml.Vector3f
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.PolydiumToggles
import org.polyfrost.polydium.client.perf.SpriteTransparency
import java.util.Optional
import java.util.function.Function

class SpriteTransparencyTest {
    private val id = Identifier.withDefaultNamespace("test")

    private fun frames(vararg colors: Int, interpolate: Boolean): SpriteContents {
        val image = NativeImage(1, colors.size, false)
        colors.forEachIndexed { y, color -> image.setPixel(0, y, color) }
        val metadata = AnimationMetadataSection(Optional.empty(), Optional.empty(), Optional.empty(), 2, interpolate)
        return SpriteContents(id, FrameSize(1, 1), image, Optional.of(metadata), emptyList(), Optional.empty())
    }

    private fun translucent(contents: SpriteContents) = contents.use { (it as SpriteTransparency.Holder).`polydium$hasTranslucent`() }

    @Test
    fun `only interpolation between clear and solid frames adds partial alpha`() {
        val clear = 0x00ffffff
        val solid = 0xffffffff.toInt()
        assertEquals(true, translucent(frames(clear, solid, interpolate = true)))
        assertEquals(false, translucent(frames(clear, solid, interpolate = false)))
        assertEquals(false, translucent(frames(solid, solid, interpolate = true)))
    }

    private fun updatedRenderType(tint: Int): Any? {
        val image = NativeImage(1, 1, false)
        image.setPixel(0, 0, 0xffffffff.toInt())
        return SpriteContents(id, FrameSize(1, 1), image).use { contents ->
            val spriteConstructor = TextureAtlasSprite::class.java.declaredConstructors.single().apply { isAccessible = true }
            val sprite = spriteConstructor.newInstance(id, contents, 16, 16, 0, 0, 0) as TextureAtlasSprite
            val point = Vector3f()
            val quad = BakedQuad(point, point, point, point, 0, 0, 0, 0, 0, Direction.NORTH, sprite, true, 0)
            val properties = ModelRenderProperties(false, sprite, ItemTransforms.NO_TRANSFORMS)
            val constructor = BlockModelWrapper::class.java.getDeclaredConstructor(List::class.java, List::class.java, ModelRenderProperties::class.java, Function::class.java).apply { isAccessible = true }
            val model = constructor.newInstance(listOf(object : ItemTintSource {
                override fun calculate(stack: ItemStack, level: ClientLevel?, entity: LivingEntity?) = tint
                override fun type() = throw UnsupportedOperationException()
            }), listOf(quad), properties, Function<Any, Any> { Sheets.translucentItemSheet() })
            val state = ItemStackRenderState()
            BlockModelWrapper::class.java.methods.single { it.name == "update" }.invoke(model, state, ItemStack(Items.STONE), null, ItemDisplayContext.GUI, null, null, 0)
            val layers = ItemStackRenderState::class.java.getDeclaredField("layers").apply { isAccessible = true }.get(state) as Array<*>
            ItemStackRenderState.LayerRenderState::class.java.getDeclaredField("renderType").apply { isAccessible = true }.get(layers[0])
        }
    }

    @Test
    fun `tint alpha keeps item blending`() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        PolydiumToggles.opaqueItemCutout = true
        try {
            assertSame(Sheets.translucentItemSheet(), updatedRenderType(0x80ffffff.toInt()))
            assertNotSame(Sheets.translucentItemSheet(), updatedRenderType(0xffffffff.toInt()))
        } finally {
            PolydiumToggles.opaqueItemCutout = false
        }
    }
}
*///?}
