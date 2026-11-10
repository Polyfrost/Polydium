package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.polyfrost.polydium.client.perf.CyclingParticleIterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

//? if >= 1.21.10 {
import net.minecraft.client.particle.ParticleGroup;

@Mixin(ParticleGroup.class)
//?} else {
/*import net.minecraft.client.particle.ParticleEngine;

@Mixin(ParticleEngine.class)
*///?}
public abstract class MixinParticleTickCycle {
    //? if >= 1.21.10 {
    @WrapOperation(method = "tickParticles", at = @At(value = "INVOKE", target = "Ljava/util/Queue;iterator()Ljava/util/Iterator;"))
    private <P> Iterator<P> polydium$cycleQueue(Queue<P> particles, Operation<Iterator<P>> original) {
    //?} else {
    /*@WrapOperation(method = "tickParticleList", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    private <P> Iterator<P> polydium$cycleQueue(Collection<P> particles, Operation<Iterator<P>> original) {
    *///?}
        Iterator<P> cycling = CyclingParticleIterator.of(particles);
        return cycling != null ? cycling : original.call(particles);
    }
}
