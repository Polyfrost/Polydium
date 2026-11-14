package org.polyfrost.polydium.mixin.client.perf.particle_core;

//? if >= 1.21.10 {
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleGroup;
import org.polyfrost.polydium.client.perf.ParticleTickOverlap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//? if >= 26.2 {
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?} else
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;*/

import java.util.Queue;

@Mixin(ParticleGroup.class)
public abstract class MixinParticleGroupVanillaOnly implements ParticleTickOverlap.VanillaOnly {
    @Shadow @Final protected Queue<? extends Particle> particles;

    @Unique private boolean polydium$mayHaveModded;

    @Inject(method = "add", at = @At("HEAD"))
    //? if >= 26.2 {
    private void polydium$flagModded(Particle particle, CallbackInfoReturnable<Boolean> cir) {
    //?} else
    /*private void polydium$flagModded(Particle particle, CallbackInfo ci) {*/
        if (!polydium$mayHaveModded && !ParticleTickOverlap.vanilla(particle))
            polydium$mayHaveModded = true;
    }

    @Override
    public boolean polydium$vanillaOnly() {
        if (!polydium$mayHaveModded)
            return true;
        for (Particle particle : particles)
            if (!ParticleTickOverlap.vanilla(particle))
                return false;
        polydium$mayHaveModded = false;
        return true;
    }
}
//?}
