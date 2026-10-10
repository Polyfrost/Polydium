package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import org.polyfrost.polydium.client.perf.QuadSort;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.util.sorting.RadixSort", remap = false)
public abstract class MixinTranslucentSortSmall {
    @Inject(method = "smallSort([I[IZ)V", at = @At("HEAD"), cancellable = true)
    private static void polydium$packedSmallSort(int[] perm, int[] keys, boolean stable, CallbackInfo ci) {
        if (stable && perm.length > 1) {
            QuadSort.sortSmall(perm, keys);
            ci.cancel();
        }
    }
}
//?}
