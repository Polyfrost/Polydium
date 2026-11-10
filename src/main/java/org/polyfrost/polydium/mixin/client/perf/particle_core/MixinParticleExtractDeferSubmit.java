package org.polyfrost.polydium.mixin.client.perf.particle_core;

//? if >= 1.21.10 {
//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
//?} else
/*import net.minecraft.client.renderer.state.ParticlesRenderState;*/
import org.polyfrost.polydium.client.perf.ParticleTickOverlap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticlesRenderState.class)
public abstract class MixinParticleExtractDeferSubmit {
    @Inject(method = "submit", at = @At("HEAD"))
    private void polydium$runDeferredParticleExtract(CallbackInfo ci) {
        ParticleTickOverlap.runDeferredExtract();
    }
}
//?}
