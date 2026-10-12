package org.polyfrost.polydium.mixin.client.perf.iris;

import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pathways.HandRenderer", remap = false)
public class MixinHandRenderer {
    @Inject(method = {"renderSolid", "renderTranslucent"}, at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void polydium$skipWithoutPack(CallbackInfo ci) {
        if (!Iris.isPackInUseQuick()) ci.cancel();
    }
}
