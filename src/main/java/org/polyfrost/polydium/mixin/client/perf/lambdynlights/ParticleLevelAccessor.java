package org.polyfrost.polydium.mixin.client.perf.lambdynlights;

//? if >= 1.21.10 {
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleLevelAccessor {
    @Accessor("level")
    ClientLevel polydium$level();
}
//?}
