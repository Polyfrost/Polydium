package org.polyfrost.polydium.mixin.client.perf.particle_core;

import net.minecraft.client.particle.Particle;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Mixin(value = Particle.class, priority = 1100)
public abstract class MixinParticleCoreLightCacheRead {
    @Redirect(method = "particle_core_tickLightUpdate", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ConcurrentHashMap;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", remap = false), remap = false, require = 0)
    private <K, V> V polydium$readFirst(ConcurrentHashMap<K, V> cache, K pos, Function<? super K, ? extends V> lookup) {
        if (!PolydiumToggles.particleLightCacheRead)
            return cache.computeIfAbsent(pos, lookup);
        V light = cache.get(pos);
        if (light != null)
            return light;
        light = lookup.apply(pos);
        V raced = cache.putIfAbsent(pos, light);
        return raced != null ? raced : light;
    }
}
