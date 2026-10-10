package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.CompactVectorArray;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
//? if >= 26.2 {
import net.minecraft.client.renderer.StagedVertexBuffer;
//?} else {
/*import com.mojang.blaze3d.vertex.MeshData;
import org.polyfrost.polydium.client.perf.SortCentroidScope;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(/*? if >= 26.2 {*/StagedVertexBuffer/*?} else {*//*MeshData*//*?}*/.class)
public class MixinSortCentroidPool {
    @Unique
    private static final int POLYDIUM_MAX_POOLED_SIZES = 256;
    @Unique
    private static final Int2ObjectOpenHashMap<CompactVectorArray> POLYDIUM_CENTROIDS = new Int2ObjectOpenHashMap<>();

    @WrapOperation(method = /*? if >= 26.2 {*/"decodeSortingPoints"/*?} else {*//*"unpackQuadCentroids"*//*?}*/, at = @At(value = "NEW", target = "(I)Lcom/mojang/blaze3d/vertex/CompactVectorArray;"))
    private static CompactVectorArray polydium$pooledCentroids(int count, Operation<CompactVectorArray> original) {
        if (!RenderSystem.isOnRenderThread()/*? if < 26.2 {*//* || !SortCentroidScope.discarded*//*?}*/) {
            return original.call(count);
        }
        CompactVectorArray centroids = POLYDIUM_CENTROIDS.get(count);
        if (centroids != null) {
            for (int i = 0; i < count; i++) {
                centroids.set(i, 0.0F, 0.0F, 0.0F);
            }
            return centroids;
        }
        if (POLYDIUM_CENTROIDS.size() >= POLYDIUM_MAX_POOLED_SIZES) {
            POLYDIUM_CENTROIDS.clear();
        }
        centroids = original.call(count);
        POLYDIUM_CENTROIDS.put(count, centroids);
        return centroids;
    }
}
//?}
