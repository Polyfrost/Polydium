package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import java.util.Arrays;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.CompactVectorArray;
import net.caffeinemc.mods.sodium.client.util.MathUtil;
import net.caffeinemc.mods.sodium.client.util.sorting.RadixSort;
import net.caffeinemc.mods.sodium.client.util.sorting.VertexSortingExtended;
import org.polyfrost.polydium.client.perf.PerfClock;
import org.polyfrost.polydium.client.perf.QuadSort;
import org.polyfrost.polydium.client.perf.SharedQuadSorter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.util.sorting.VertexSorters$AbstractSorter", remap = false)
public abstract class MixinSortReuse implements SharedQuadSorter {
    @WrapOperation(
            method = "sort(Lcom/mojang/blaze3d/vertex/CompactVectorArray;)[I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/util/sorting/RadixSort;sortIndirect([I[IZ)V"
            )
    )
    private void polydium$reuseOrder(int[] perm, int[] keys, boolean stable, Operation<Void> original) {
        if (!stable || !RenderSystem.isOnRenderThread()) {
            original.call(perm, keys, stable);
            return;
        }
        if (QuadSort.reuse(PerfClock.frame, perm, keys)) {
            return;
        }
        original.call(perm, keys, stable);
        QuadSort.remember(perm);
    }

    @Override
    public int[] polydium$sortShared(CompactVectorArray centroids) {
        VertexSortingExtended sorting = (VertexSortingExtended) (Object) this;
        int length = centroids.size();
        int[] keys = QuadSort.keys(length);
        for (int index = 0; index < length; index++) {
            keys[index] = ~MathUtil.floatToComparableInt(sorting.applyMetric(centroids.getX(index), centroids.getY(index), centroids.getZ(index)));
        }
        int[] order = QuadSort.reuseShared(PerfClock.frame, keys, length);
        if (order != null) {
            return order;
        }
        int[] perm = new int[length];
        for (int index = 0; index < length; index++) {
            perm[index] = index;
        }
        RadixSort.sortIndirect(perm, Arrays.copyOf(keys, length), true);
        QuadSort.rememberOwned(perm);
        return perm;
    }
}
//?}
