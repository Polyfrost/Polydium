package org.polyfrost.polydium.mixin.client.perf;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class MixinSpriteMipRelease {
    @Inject(method = "upload", at = @At("RETURN"))
    private void polydium$releaseUploadedMips(SpriteLoader.Preparations preparations, CallbackInfo ci) {
        if (!PolydiumToggles.spriteMipRelease || preparations.mipLevel() == 0) {
            return;
        }

        for (TextureAtlasSprite sprite : preparations.regions().values()) {
            SpriteContents contents = sprite.contents();

            //? if < 1.21.11 {
            /*
            if (contents.getUniqueFrames().count() > 1L) {
                continue;
            }
            *///?}

            SpriteContentsMipAccessor accessor = (SpriteContentsMipAccessor) contents;
            NativeImage[] levels = accessor.polydium$byMipLevel();

            if (levels.length <= 1) {
                continue;
            }

            for (int level = 1; level < levels.length; level++) {
                levels[level].close();
            }

            accessor.polydium$setByMipLevel(new NativeImage[]{levels[0]});
        }
    }
}
