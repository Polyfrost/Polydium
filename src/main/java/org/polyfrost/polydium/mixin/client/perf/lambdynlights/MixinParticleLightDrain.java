package org.polyfrost.polydium.mixin.client.perf.lambdynlights;

//? if >= 1.21.10 {
import net.minecraft.client.particle.ParticleEngine;
import org.polyfrost.polydium.client.perf.ParticleLightBatch;
import org.polyfrost.polydium.client.perf.ParticleTickOverlap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleLightDrain {
    @Inject(method = "tick", at = @At("RETURN"))
    private void polydium$drainParticleLights(CallbackInfo ci) {
        if (!ParticleTickOverlap.pending())
            ParticleLightBatch.drain();
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void polydium$dropStaleParticleLights(CallbackInfo ci) {
        ParticleLightBatch.drainOverflow();
    }
}
//?}
