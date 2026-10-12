package org.polyfrost.polydium.mixin.client.perf;

//? if < 1.21.4 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.SinglePassTextShadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Font.class)
public abstract class MixinSinglePassTextShadow {
    @WrapOperation(
            method = "drawInternal(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;renderText(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F")
    )
    private float polydium$singlePassString(Font font, String text, float x, float y, int color, boolean dropShadow, Matrix4f pose, MultiBufferSource buffers, Font.DisplayMode mode, int background, int light, Operation<Float> original) {
        if (dropShadow && polydium$singlePass(pose, mode, background)) {
            return 0.0F;
        }
        try {
            return original.call(font, text, x, y, color, dropShadow, pose, buffers, mode, background, light);
        } finally {
            SinglePassTextShadow.take();
        }
    }

    @WrapOperation(
            method = "drawInternal(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F")
    )
    private float polydium$singlePassSequence(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, Matrix4f pose, MultiBufferSource buffers, Font.DisplayMode mode, int background, int light, Operation<Float> original) {
        if (dropShadow && polydium$singlePass(pose, mode, background)) {
            return 0.0F;
        }
        try {
            return original.call(font, text, x, y, color, dropShadow, pose, buffers, mode, background, light);
        } finally {
            SinglePassTextShadow.take();
        }
    }

    @Unique
    private static boolean polydium$singlePass(Matrix4f pose, Font.DisplayMode mode, int background) {
        if (!PolydiumToggles.singlePassTextShadow || background != 0 || mode == Font.DisplayMode.SEE_THROUGH) {
            return false;
        }
        SinglePassTextShadow.offer(pose);
        return true;
    }
}
*///?}
