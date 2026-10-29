package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 && < 26.2 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import org.polyfrost.polydium.client.perf.FenceBatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public abstract class MixinFenceBatchFlush {
    @Inject(method = "flipFrame", at = @At("HEAD"))
    private static void polydium$flushFences(CallbackInfo ci) {
        FenceBatcher.flush();
    }
}
*///?}
