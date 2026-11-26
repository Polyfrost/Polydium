package org.polyfrost.polydium.mixin.client.perf.iris;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.config.IrisConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.Iris", remap = false)
public class MixinIrisReloadWithoutPack {
    @Inject(method = "reload", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/config/IrisConfig;initialize()V", shift = At.Shift.AFTER), cancellable = true, require = 0)
    private static void polydium$skipReloadWithoutPack(CallbackInfo ci) {
        IrisConfig config = Iris.getIrisConfig();
        if (Iris.getCurrentPack().isEmpty() && !Iris.isFallback() && (!config.areShadersEnabled() || config.getShaderPackName().isEmpty()))
            ci.cancel();
    }
}
