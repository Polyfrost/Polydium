package org.polyfrost.polydium.mixin.client.perf.particle_core;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.ParticleEngine;
import org.polyfrost.polydium.client.perf.DeadParticleIterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;
import java.util.Queue;

@Mixin(value = ParticleEngine.class, priority = 200001)
public abstract class MixinParticleCoreFinalizeDead {
    @WrapOperation(method = "finalizeParticles", at = @At(value = "INVOKE", target = "Ljava/util/Queue;iterator()Ljava/util/Iterator;"), require = 0)
    private <P> Iterator<P> polydium$deadOnlyFinalize(Queue<P> particles, Operation<Iterator<P>> original) {
        Iterator<P> dead = DeadParticleIterator.of(particles);
        return dead != null ? dead : original.call(particles);
    }
}
//?}
