package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 && < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.core.particles.ParticleLimit;
import org.polyfrost.polydium.client.perf.ParticleReservoir;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleReservoirCap {
    @Shadow protected abstract void updateCount(ParticleLimit limit, int change);

    @WrapOperation(method = "tick", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleGroup;add(Lnet/minecraft/client/particle/Particle;)V"))
    private void polydium$reservoirCap(ParticleGroup<?> group, Particle particle, Operation<Void> original) {
        if (ParticleReservoir.accepts(group.getAll())) {
            original.call(group, particle);
        } else {
            particle.getParticleLimit().ifPresent(limit -> this.updateCount(limit, -1));
        }
    }
}
*///?}
