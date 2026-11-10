package org.polyfrost.polydium.mixin.client.perf.sodium;

import org.polyfrost.polydium.client.perf.SectionStates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection", remap = false)
public abstract class MixinSharedStateUnpackSection implements SectionStates.Access {
    @Unique
    private volatile SectionStates.Shared polydium$states;

    @Override
    public SectionStates.Shared polydium$states() {
        return this.polydium$states;
    }

    @Override
    public void polydium$states(SectionStates.Shared states) {
        this.polydium$states = states;
    }
}
