package org.polyfrost.polydium.mixin.client.perf.lambdynlights;

//? if >= 26.1 {
import net.minecraft.client.particle.Particle;
import org.polyfrost.polydium.client.perf.ParticleLightBatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.lambdaurora.lambdynlights.engine.source.ParticleLightSourceBehavior", remap = false)
public abstract class MixinParticleLightBatch {
    @Inject(method = "tickParticle", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void polydium$batchOffThread(Particle particle, CallbackInfo ci) {
        if (ParticleLightBatch.defer(particle))
            ci.cancel();
    }
}
//?}
