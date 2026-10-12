package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if > 1.8.9 && < 26.1 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import org.polyfrost.polydium.client.perf.PreciseFrameLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public abstract class MixinPreciseFrameLimiter {
    @Inject(method = "limitDisplayFPS", at = @At("HEAD"), cancellable = true)
    private static void polydium$preciseLimit(int fps, CallbackInfo ci) {
        if (PolydiumToggles.preciseFrameLimiter && fps > 0) {
            PreciseFrameLimiter.limit(fps);
            ci.cancel();
        }
    }
}
*///?}
