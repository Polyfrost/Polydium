package org.polyfrost.polydium.mixin.client.perf;

//? if < 26.1 {
/*import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.polyfrost.polydium.client.perf.SpriteTransparency;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpriteContents.class)
public abstract class MixinSpriteContentsTransparency implements SpriteTransparency.Holder {
    @Shadow
    @Final
    private NativeImage originalImage;

    @Unique
    private byte polydium$translucent;

    @Override
    public boolean polydium$hasTranslucent() {
        byte state = this.polydium$translucent;
        if (state == 0) {
            state = polydium$scan(this.originalImage) ? (byte) 1 : (byte) -1;
            this.polydium$translucent = state;
        }
        return state > 0;
    }

    @Unique
    private static boolean polydium$scan(NativeImage image) {
        if (image.format() != NativeImage.Format.RGBA) {
            return false;
        }
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
            }
        }
        return false;
    }
}
*///?}
