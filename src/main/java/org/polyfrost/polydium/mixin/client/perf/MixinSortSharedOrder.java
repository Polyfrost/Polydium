package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.CompactVectorArray;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.polyfrost.polydium.client.perf.SharedQuadSorter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MeshData.SortState.class)
public class MixinSortSharedOrder {
    @WrapOperation(
        method = /*? if >= 26.2 {*/"writeSortedIndexBuffer"/*?} else {*//*"buildSortedIndexBuffer"*//*?}*/,
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexSorting;sort(Lcom/mojang/blaze3d/vertex/CompactVectorArray;)[I")
    )
    private int[] polydium$sharedOrder(VertexSorting sorting, CompactVectorArray centroids, Operation<int[]> original) {
        if (sorting instanceof SharedQuadSorter shared && RenderSystem.isOnRenderThread()) {
            return shared.polydium$sortShared(centroids);
        }
        return original.call(sorting, centroids);
    }
}
//?}
