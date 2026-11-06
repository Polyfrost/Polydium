package org.polyfrost.polydium.mixin.client.perf;

//? if < 26.1 {
/*import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import org.polyfrost.polydium.client.perf.SpriteTransparency;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteContents.class)
public abstract class MixinSpriteContentsTransparency implements SpriteTransparency.Holder {
    @Shadow
    @Final
    private NativeImage originalImage;

    @Unique
    private byte polydium$translucent;

    @Unique
    private boolean polydium$interpolated;

    @Inject(method = "createAnimatedTexture", at = @At("HEAD"))
    private void polydium$readInterpolation(CallbackInfoReturnable<?> cir, @Local(argsOnly = true) AnimationMetadataSection section) {
        //? if >= 1.21.4 {
        this.polydium$interpolated = section.interpolatedFrames();
        //?} else {
        /^this.polydium$interpolated = section.isInterpolatedFrames();
        ^///?}
    }

    @Override
    public boolean polydium$hasTranslucent() {
        byte state = this.polydium$translucent;
        if (state == 0) {
            state = polydium$scan(this.originalImage, this.polydium$interpolated) ? (byte) 1 : (byte) -1;
            this.polydium$translucent = state;
        }
        return state > 0;
    }

    @Unique
    private static boolean polydium$scan(NativeImage image, boolean interpolated) {
        if (image.format() != NativeImage.Format.RGBA) {
            return false;
        }
        boolean clear = false, solid = false;
        int width = image.getWidth(), height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //? if >= 1.21.4 {
                int alpha = image.getPixel(x, y) >>> 24;
                //?} else {
                /^int alpha = image.getPixelRGBA(x, y) >>> 24;
                ^///?}
                if (alpha != 0 && alpha != 255) {
                    return true;
                }
                clear |= alpha == 0;
                solid |= alpha == 255;
            }
        }
        return interpolated && clear && solid;
    }
}
*///?}
