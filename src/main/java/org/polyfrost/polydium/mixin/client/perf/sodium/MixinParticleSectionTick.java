package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import net.minecraft.client.particle.SingleQuadParticle;
import org.polyfrost.polydium.client.perf.ParticleSectionVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Ported from Argentum (ParticleMixin) with permission from rdh
@Mixin(SingleQuadParticle.class)
public class MixinParticleSectionTick implements ParticleSectionVisibility.Access {
    @Unique
    private long polydium$sectionVisibleTick = -1L;

    @Override
    public long polydium$sectionVisibleTick() {
        return this.polydium$sectionVisibleTick;
    }

    @Override
    public void polydium$sectionVisibleTick(long tick) {
        this.polydium$sectionVisibleTick = tick;
    }
}
//?}
