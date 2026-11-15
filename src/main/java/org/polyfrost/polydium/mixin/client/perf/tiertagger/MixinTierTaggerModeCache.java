package org.polyfrost.polydium.mixin.client.perf.tiertagger;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Pseudo
@Mixin(targets = {"net.uku3lig.tiertagger.TierCache", "com.kevin.tiertagger.TierCache"}, remap = false)
public class MixinTierTaggerModeCache {
    @Unique
    private static volatile Map<String, Optional<?>> polydium$modes = new ConcurrentHashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapMethod(method = "findMode", remap = false, require = 0)
    private static Optional polydium$cachedMode(String id, Operation<Optional> original) {
        if (id == null) return original.call(id);
        Map<String, Optional<?>> modes = polydium$modes;
        Optional<?> mode = modes.get(id);
        if (mode == null) {
            mode = original.call(id);
            modes.put(id, mode);
        }
        return mode;
    }

    @Inject(method = "init", at = @At("RETURN"), remap = false, require = 0)
    private static void polydium$resetModes(CallbackInfo ci) {
        polydium$modes = new ConcurrentHashMap<>();
    }
}
