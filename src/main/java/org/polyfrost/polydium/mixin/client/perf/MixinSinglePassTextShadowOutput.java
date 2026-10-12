package org.polyfrost.polydium.mixin.client.perf;

//? if < 1.21.4 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.joml.Matrix4f;
import org.polyfrost.polydium.client.perf.SinglePassTextShadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class MixinSinglePassTextShadowOutput {
    @Unique private Matrix4f polydium$shadowPose;
    @Unique private float polydium$shadowOffset = 1.0F;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$takeShadowPose(CallbackInfo ci) {
        this.polydium$shadowPose = SinglePassTextShadow.take();
    }

    @WrapOperation(
            method = "accept",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;renderChar(Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;ZZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFI)V")
    )
    private void polydium$renderShadowWithGlyph(Font font, BakedGlyph glyph, boolean bold, boolean italic, float boldOffset, float x, float y, Matrix4f pose, VertexConsumer buffer, float red, float green, float blue, float alpha, int light, Operation<Void> original, @Local GlyphInfo info) {
        Matrix4f shadowPose = this.polydium$shadowPose;
        if (shadowPose != null) {
            float offset = this.polydium$shadowOffset = info.getShadowOffset();
            original.call(font, glyph, bold, italic, boldOffset, x + offset, y + offset, shadowPose, buffer, red * 0.25F, green * 0.25F, blue * 0.25F, alpha, light);
        }
        original.call(font, glyph, bold, italic, boldOffset, x, y, pose, buffer, red, green, blue, alpha, light);
    }

    @WrapOperation(
            method = "finish",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;renderEffect(Lnet/minecraft/client/gui/font/glyphs/BakedGlyph$Effect;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;I)V")
    )
    private void polydium$renderShadowWithEffect(BakedGlyph white, BakedGlyph.Effect effect, Matrix4f pose, VertexConsumer buffer, int light, Operation<Void> original) {
        Matrix4f shadowPose = this.polydium$shadowPose;
        if (shadowPose != null) {
            BakedGlyphEffectAccessor main = (BakedGlyphEffectAccessor) (Object) effect;
            float offset = this.polydium$shadowOffset;
            original.call(white, new BakedGlyph.Effect(main.polydium$x0() + offset, main.polydium$y0() + offset, main.polydium$x1() + offset, main.polydium$y1() + offset, main.polydium$depth(),
                    main.polydium$r() * 0.25F, main.polydium$g() * 0.25F, main.polydium$b() * 0.25F, main.polydium$a()), shadowPose, buffer, light);
        }
        original.call(white, effect, pose, buffer, light);
    }
}
*///?}
