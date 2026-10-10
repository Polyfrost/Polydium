package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.GlyphVertex;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;
import org.polyfrost.polydium.client.perf.DirectVertexBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BakedSheetGlyph.class)
public abstract class MixinDirectVertexGlyph {
    @Unique private static final int POLYDIUM_TEXT_STRIDE = 24;

    @Shadow @Final private float left;
    @Shadow @Final private float right;
    @Shadow @Final private float up;
    @Shadow @Final private float down;
    @Shadow @Final private float u0;
    @Shadow @Final private float u1;
    @Shadow @Final private float v0;
    @Shadow @Final private float v1;

    @WrapOperation(
            method = "renderChar",
            //? if >= 26.1 {
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/BakedSheetGlyph;render(ZFFFLorg/joml/Matrix4fc;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V")
    )
    private void polydium$writeGlyphIntoTarget(BakedSheetGlyph glyph, boolean italic, float x, float y, float z, Matrix4fc pose,
            //?} else {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/BakedSheetGlyph;render(ZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V")
    )
    private void polydium$writeGlyphIntoTarget(BakedSheetGlyph glyph, boolean italic, float x, float y, float z, org.joml.Matrix4f pose,
            *///?}
                                               VertexConsumer builder, int color, boolean bold, int light, Operation<Void> original) {
        long pointer = DirectVertexBuffers.reserve(builder, GlyphVertex.FORMAT, GlyphVertex.STRIDE, 4);
        if (pointer != 0L) {
            this.polydium$writeQuad(pointer, true, italic, x, y, z, pose, color, bold, light);
            return;
        }
        pointer = DirectVertexBuffers.reserve(builder, DefaultVertexFormat.POSITION_TEX_COLOR, POLYDIUM_TEXT_STRIDE, 4);
        if (pointer != 0L) {
            this.polydium$writeQuad(pointer, false, italic, x, y, z, pose, color, bold, light);
            return;
        }
        VertexBufferWriter writer = VertexBufferWriter.tryOf(builder);
        if (writer == null) {
            original.call(glyph, italic, x, y, z, pose, builder, color, bold, light);
            return;
        }
        boolean lit = DirectVertexBuffers.formatOf(builder) != DefaultVertexFormat.POSITION_TEX_COLOR;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long buffer = stack.nmalloc(4 * GlyphVertex.STRIDE);
            this.polydium$writeQuad(buffer, lit, italic, x, y, z, pose, color, bold, light);
            writer.push(stack, buffer, 4, lit ? GlyphVertex.FORMAT : DefaultVertexFormat.POSITION_TEX_COLOR);
        }
    }

    @Unique
    private void polydium$writeQuad(long pointer, boolean lit, boolean italic, float x, float y, float z, Matrix4fc pose, int color, boolean bold, int light) {
        float x1 = x + this.left;
        float x2 = x + this.right;
        float h1 = y + this.up;
        float h2 = y + this.down;
        float w1 = italic ? 1.0F - 0.25F * this.up : 0.0F;
        float w2 = italic ? 1.0F - 0.25F * this.down : 0.0F;
        float offset = bold ? 0.1F : 0.0F;
        int abgr = ColorARGB.toABGR(color);
        long stride = lit ? GlyphVertex.STRIDE : POLYDIUM_TEXT_STRIDE;
        polydium$put(pointer, lit, pose, x1 + w1 - offset, h1 - offset, z, abgr, this.u0, this.v0, light);
        polydium$put(pointer + stride, lit, pose, x1 + w2 - offset, h2 + offset, z, abgr, this.u0, this.v1, light);
        polydium$put(pointer + 2L * stride, lit, pose, x2 + w2 + offset, h2 + offset, z, abgr, this.u1, this.v1, light);
        polydium$put(pointer + 3L * stride, lit, pose, x2 + w1 + offset, h1 - offset, z, abgr, this.u1, this.v0, light);
    }

    @Unique
    private static void polydium$put(long pointer, boolean lit, Matrix4fc pose, float x, float y, float z, int color, float u, float v, int light) {
        float xt = MatrixHelper.transformPositionX(pose, x, y, z);
        float yt = MatrixHelper.transformPositionY(pose, x, y, z);
        float zt = MatrixHelper.transformPositionZ(pose, x, y, z);
        if (lit) {
            GlyphVertex.put(pointer, xt, yt, zt, color, u, v, light);
        } else {
            PositionAttribute.put(pointer, xt, yt, zt);
            TextureAttribute.put(pointer + 12L, u, v);
            ColorAttribute.set(pointer + 20L, color);
        }
    }
}
//?}
