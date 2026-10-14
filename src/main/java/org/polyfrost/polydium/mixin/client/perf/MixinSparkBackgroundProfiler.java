package org.polyfrost.polydium.mixin.client.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.lucko.spark.common.sampler.BackgroundSamplerManager", remap = false)
public abstract class MixinSparkBackgroundProfiler {
    @Inject(method = "initialise", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polydium$skip(CallbackInfo ci) {
        ci.cancel();
    }
}
