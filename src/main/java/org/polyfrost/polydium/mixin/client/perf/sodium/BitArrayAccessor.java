package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.util.collections.BitArray", remap = false)
public interface BitArrayAccessor {
    @Accessor(value = "words", remap = false)
    long[] polydium$words();
}
//?}
