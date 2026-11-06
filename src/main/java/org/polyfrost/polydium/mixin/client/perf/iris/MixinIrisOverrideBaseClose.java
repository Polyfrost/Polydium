package org.polyfrost.polydium.mixin.client.perf.iris;

//? if >= 26.3 {
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import org.polyfrost.polydium.client.perf.IrisOverrideSweep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FrontendRenderPipeline.class)
public abstract class MixinIrisOverrideBaseClose {
    @Inject(method = "close", at = @At("HEAD"))
    private void polydium$sweepOverrides(CallbackInfo ci) {
        IrisOverrideSweep.baseClosed();
    }
}
//?}
