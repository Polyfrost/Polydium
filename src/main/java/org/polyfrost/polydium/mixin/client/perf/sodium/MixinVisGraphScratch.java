package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import net.caffeinemc.mods.sodium.client.util.collections.BitArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.occlusion.DirectionalVisGraph", remap = false)
public abstract class MixinVisGraphScratch {
    @Unique
    private BitArray polydium$visited;

    @Redirect(method = "resolveWithDirections", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/util/collections/BitArray;copy()Lnet/caffeinemc/mods/sodium/client/util/collections/BitArray;"))
    private BitArray polydium$copyIntoScratch(BitArray blocks) {
        BitArray visited = this.polydium$visited;
        if (visited == null) {
            return this.polydium$visited = blocks.copy();
        }
        long[] from = ((BitArrayAccessor) blocks).polydium$words();
        System.arraycopy(from, 0, ((BitArrayAccessor) visited).polydium$words(), 0, from.length);
        return visited;
    }
}
//?}
