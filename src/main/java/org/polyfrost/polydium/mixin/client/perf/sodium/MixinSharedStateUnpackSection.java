package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import net.minecraft.world.level.block.state.BlockState;
import org.polyfrost.polydium.client.perf.SectionStates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection", remap = false)
public abstract class MixinSharedStateUnpackSection implements SectionStates.Access {
    @Unique
    private volatile BlockState[] polydium$states;

    @Override
    public BlockState[] polydium$states() {
        return this.polydium$states;
    }

    @Override
    public void polydium$states(BlockState[] states) {
        this.polydium$states = states;
    }
}
//?}
