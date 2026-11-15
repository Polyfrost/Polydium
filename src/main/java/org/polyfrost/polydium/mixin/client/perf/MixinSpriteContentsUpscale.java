package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.renderer.texture.SpriteContents;
import org.polyfrost.polydium.client.perf.UpscaledSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpriteContents.class)
public abstract class MixinSpriteContentsUpscale implements UpscaledSprite {
    @Unique
    private int polydium$upscale = 1;

    @Override
    public int polydium$upscale() {
        return this.polydium$upscale;
    }

    @Override
    public void polydium$setUpscale(int factor) {
        this.polydium$upscale = factor;
    }
}
