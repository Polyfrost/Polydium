package org.polyfrost.polydium.mixin.client.perf.particle_core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.ParticleEngine;
import org.polyfrost.polydium.client.perf.CyclingParticleIterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Iterator;
//? if >= 1.21.10
import java.util.Queue;

@Mixin(value = ParticleEngine.class, priority = 200000)
public abstract class MixinParticleCoreFinalizeCycle {
    //? if >= 1.21.10 {
    @WrapOperation(method = "finalizeParticles", at = @At(value = "INVOKE", target = "Ljava/util/Queue;iterator()Ljava/util/Iterator;"), require = 0)
    private <P> Iterator<P> polydium$cycleFinalizeQueue(Queue<P> particles, Operation<Iterator<P>> original) {
        Iterator<P> cycling = CyclingParticleIterator.of(particles);
        return cycling != null ? cycling : original.call(particles);
    }
    //?}

    @WrapOperation(method = "finalizeParticles", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"), require = 0)
    private <P> Iterator<P> polydium$cycleFinalizeCollection(Collection<P> particles, Operation<Iterator<P>> original) {
        Iterator<P> cycling = CyclingParticleIterator.of(particles);
        return cycling != null ? cycling : original.call(particles);
    }
}
