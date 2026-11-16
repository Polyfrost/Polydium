package org.polyfrost.polydium.mixin.client.perf.etf;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Pseudo
@Mixin(targets = "traben.entity_texture_features.features.state.ETFSubmitData", remap = false)
public abstract class MixinEtfSubmitDataMap {
    @Shadow
    @Final
    @Mutable
    public Map<String, Object> data;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false, require = 0)
    private void polydium$arrayMap(CallbackInfo ci) {
        this.data = new Object2ObjectArrayMap<>();
    }
}
