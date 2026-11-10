package org.polyfrost.polydium.mixin.client.perf.sodium;

import org.polyfrost.polydium.client.perf.ModernFixModelCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer", remap = false)
public abstract class MixinModernFixModelCacheReset {
    @Inject(method = "initRenderer", at = @At("RETURN"))
    private void polydium$resetModernFixModels(CallbackInfo ci) {
        ModernFixModelCache.reset();
    }
}
