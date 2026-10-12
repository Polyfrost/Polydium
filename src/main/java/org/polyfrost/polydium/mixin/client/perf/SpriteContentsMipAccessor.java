package org.polyfrost.polydium.mixin.client.perf;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteContents.class)
public interface SpriteContentsMipAccessor {
    @Accessor("byMipLevel")
    NativeImage[] polydium$byMipLevel();

    @Accessor("byMipLevel")
    void polydium$setByMipLevel(NativeImage[] levels);
}
