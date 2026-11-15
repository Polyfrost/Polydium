package org.polyfrost.polydium.mixin.client.perf;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpriteContents.class)
public interface SpriteContentsAccessor {
    @Accessor("originalImage")
    NativeImage polydium$originalImage();

    @Mutable
    @Accessor("originalImage")
    void polydium$setOriginalImage(NativeImage image);

    @Accessor("byMipLevel")
    void polydium$setByMipLevel(NativeImage[] images);

    @Mutable
    @Accessor("width")
    void polydium$setWidth(int width);

    @Mutable
    @Accessor("height")
    void polydium$setHeight(int height);

    @Invoker("getFrameCount")
    int polydium$frameCount();
}
