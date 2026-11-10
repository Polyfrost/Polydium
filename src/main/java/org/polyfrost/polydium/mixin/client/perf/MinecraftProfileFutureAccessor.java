package org.polyfrost.polydium.mixin.client.perf;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftProfileFutureAccessor {
    @Accessor("profileFuture")
    CompletableFuture<?> polydium$profileFuture();
}
