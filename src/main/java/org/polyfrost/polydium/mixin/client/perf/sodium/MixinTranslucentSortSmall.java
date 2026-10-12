package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 || < 1.21.4 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.polyfrost.polydium.client.perf.QuadSort;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.util.sorting.RadixSort", remap = false)
public abstract class MixinTranslucentSortSmall {
    @WrapMethod(method = "smallSort([I[IZ)V")
    private static void polydium$packedSmallSort(int[] perm, int[] keys, boolean stable, Operation<Void> original) {
        if (stable && perm.length > 1) {
            QuadSort.sortSmall(perm, keys);
        } else {
            original.call(perm, keys, stable);
        }
    }
}
//?}
