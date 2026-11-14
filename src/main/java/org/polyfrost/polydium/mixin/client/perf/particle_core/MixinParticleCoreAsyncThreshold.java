package org.polyfrost.polydium.mixin.client.perf.particle_core;

//? if >= 1.21.10 {
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ParticleEngine.class, priority = 200003)
public abstract class MixinParticleCoreAsyncThreshold {
}
//?}
