package org.polyfrost.polydium.mixin.client.perf;

//? if < 1.21.4 {
/*import net.minecraft.client.renderer.texture.HttpTexture;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(HttpTexture.class)
public interface HttpTextureAccessor {
    @Accessor("file") @Nullable File polydium$file();
}
*///?}
