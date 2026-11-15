package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.StitcherException;
import org.polyfrost.polydium.client.perf.UpscaledSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Vanilla lowers the mip level of a whole atlas to fit its least power-of-two sprite, so one odd-sized texture from a
 * resource pack (a 3x4 vine leaf, say) turns off mipmaps for every block and distant terrain aliases. Static sprites
 * that would cap the level are scaled up by an integer factor with nearest sampling instead, which looks the same at
 * mip 0 because sprite UVs are normalised to the sprite's atlas rectangle.
 */
@Mixin(SpriteLoader.class)
public abstract class MixinSpriteMipUpscale {
    @Unique
    private static final int MAX_SIZE = 1024;
    // 256 MiB of RGBA across all scaled copies, which stay alive with the atlas
    @Unique
    private static final long MAX_SCALED_PIXELS = 1L << 26;

    // Upscaling raises the atlas mip level, and with it the per-sprite padding, so an atlas that fit before can overflow.
    // Then the original images go back and vanilla stitches them as it would have without this.
    @WrapMethod(method = "stitch")
    private SpriteLoader.Preparations polydium$upscaleCappingSprites(List<SpriteContents> sprites, int maxMipmapLevels, Executor executor, Operation<SpriteLoader.Preparations> original) {
        int target = maxMipmapLevels;
        int lowestUnscaled = 1 << maxMipmapLevels;

        for (SpriteContents sprite : sprites) {
            int lowest = polydium$lowestOneBit(sprite);
            lowestUnscaled = Math.min(lowestUnscaled, lowest);
            if (lowest < 1 << maxMipmapLevels)
                target = Math.min(target, Integer.numberOfTrailingZeros(lowest * polydium$maxFactor(sprite)));
        }

        if (target <= Integer.numberOfTrailingZeros(lowestUnscaled))
            return original.call(sprites, maxMipmapLevels, executor);

        int needed = 1 << target;
        long pixels = 0;
        for (SpriteContents sprite : sprites) {
            int factor = needed / polydium$lowestOneBit(sprite);
            if (factor > 1)
                pixels += (long) sprite.width() * sprite.height() * factor * factor;
        }

        if (pixels > MAX_SCALED_PIXELS)
            return original.call(sprites, maxMipmapLevels, executor);

        List<SpriteContents> scaledSprites = new ArrayList<>();
        List<NativeImage> sources = new ArrayList<>();

        try {
            for (SpriteContents sprite : sprites) {
                int lowest = polydium$lowestOneBit(sprite);
                if (lowest >= needed)
                    continue;

                int factor = needed / lowest;
                SpriteContentsAccessor access = (SpriteContentsAccessor) sprite;
                NativeImage source = access.polydium$originalImage();
                NativeImage scaled = new NativeImage(sprite.width() * factor, sprite.height() * factor, false);
                polydium$setImage(access, scaled);
                ((UpscaledSprite) sprite).polydium$setUpscale(factor);
                scaledSprites.add(sprite);
                sources.add(source);

                for (int y = 0; y < scaled.getHeight(); y++) {
                    for (int x = 0; x < scaled.getWidth(); x++) {
                        //? if >= 1.21.4 {
                        scaled.setPixel(x, y, source.getPixel(x / factor, y / factor));
                        //?} else {
                        /*scaled.setPixelRGBA(x, y, source.getPixelRGBA(x / factor, y / factor));
                        *///?}
                    }
                }
            }
        } catch (RuntimeException e) {
            polydium$restore(scaledSprites, sources);
            return original.call(sprites, maxMipmapLevels, executor);
        }

        SpriteLoader.Preparations result;
        try {
            result = original.call(sprites, maxMipmapLevels, executor);
        } catch (ReportedException e) {
            if (!(e.getCause() instanceof StitcherException))
                throw e;
            polydium$restore(scaledSprites, sources);
            return original.call(sprites, maxMipmapLevels, executor);
        }

        sources.forEach(NativeImage::close);
        return result;
    }

    @Unique
    private static void polydium$restore(List<SpriteContents> scaledSprites, List<NativeImage> sources) {
        for (int i = 0; i < scaledSprites.size(); i++) {
            SpriteContentsAccessor access = (SpriteContentsAccessor) scaledSprites.get(i);
            NativeImage scaled = access.polydium$originalImage();
            polydium$setImage(access, sources.get(i));
            ((UpscaledSprite) scaledSprites.get(i)).polydium$setUpscale(1);
            scaled.close();
        }
    }

    @Unique
    private static int polydium$lowestOneBit(SpriteContents sprite) {
        return Math.min(Integer.lowestOneBit(sprite.width()), Integer.lowestOneBit(sprite.height()));
    }

    @Unique
    private static int polydium$maxFactor(SpriteContents sprite) {
        SpriteContentsAccessor access = (SpriteContentsAccessor) sprite;
        NativeImage source = access.polydium$originalImage();
        int size = Math.max(sprite.width(), sprite.height());
        if (access.polydium$frameCount() > 1 || size > MAX_SIZE || source.getWidth() != sprite.width() || source.getHeight() != sprite.height() || source.format() != NativeImage.Format.RGBA)
            return 1;
        return Integer.highestOneBit(MAX_SIZE / size);
    }

    @Unique
    private static void polydium$setImage(SpriteContentsAccessor access, NativeImage image) {
        access.polydium$setWidth(image.getWidth());
        access.polydium$setHeight(image.getHeight());
        access.polydium$setOriginalImage(image);
        access.polydium$setByMipLevel(new NativeImage[]{image});
    }
}
