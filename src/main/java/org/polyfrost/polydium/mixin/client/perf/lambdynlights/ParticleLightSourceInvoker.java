package org.polyfrost.polydium.mixin.client.perf.lambdynlights;

//? if >= 26.1 {
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "dev.lambdaurora.lambdynlights.engine.source.ParticleLightSourceBehavior", remap = false)
public interface ParticleLightSourceInvoker {
    @Invoker(value = "doTickParticle", remap = false)
    static void polydium$doTickParticle(Particle particle) {
        throw new AssertionError();
    }
}
//?}
