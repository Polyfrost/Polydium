package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if > 1.8.9 && < 26.1 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import org.polyfrost.polydium.client.perf.PreciseFrameLimiter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderSystem.class)
public abstract class MixinPreciseFrameLimiter {
    @WrapMethod(method = "limitDisplayFPS")
    private static void polydium$preciseLimit(int fps, Operation<Void> original) {
        if (PolydiumToggles.preciseFrameLimiter && fps > 0) {
            PreciseFrameLimiter.limit(fps);
        } else {
            original.call(fps);
        }
    }
}
*///?}
