package org.polyfrost.polydium.mixin.client.perf.iris;

//? if >= 26.3 {
import org.polyfrost.polydium.client.perf.IrisOverrideSweep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.IrisRenderingPipeline", remap = false)
public class MixinIrisRenderingPipeline {
    @Inject(method = "destroy", at = @At("HEAD"), require = 0, expect = 0)
    private void polydium$trackDestroyed(CallbackInfo ci) {
        IrisOverrideSweep.destroying(this);
    }
}
//?}
