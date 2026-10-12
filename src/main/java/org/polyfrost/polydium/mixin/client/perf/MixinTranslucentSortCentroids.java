package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
//? if >= 1.21.10 {
import com.mojang.blaze3d.vertex.CompactVectorArray;
//?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.joml.Vector3f;
import org.polyfrost.polydium.client.perf.SortCentroidScope;
*///?}
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
//? if >= 26.2 || < 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//?} else {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Mixin(MeshData.class)
public abstract class MixinTranslucentSortCentroids {
    //? if >= 26.2 {
    @WrapMethod(method = "decodeQuadCentroids")
    private static void polydium$decodeFromMemory(ByteBuffer buffer, int vertexCount, VertexFormat format, CompactVectorArray centroids, int firstQuad, Operation<Void> original) {
        VertexFormatElement position = format.getElement("Position");
        if (position == null || !polydium$decode(buffer, vertexCount, format.getVertexSize(), position.offset(), centroids, firstQuad)) {
            original.call(buffer, vertexCount, format, centroids, firstQuad);
        }
    }
    //?} else if >= 1.21.10 {
    /*@ModifyExpressionValue(method = "unpackQuadCentroids", at = @At(value = "NEW", target = "(I)Lcom/mojang/blaze3d/vertex/CompactVectorArray;"))
    private static CompactVectorArray polydium$decodeFromMemory(CompactVectorArray centroids, @Local(argsOnly = true) ByteBuffer buffer, @Local(argsOnly = true) int vertexCount, @Local(argsOnly = true) VertexFormat format,
                                                                 @Cancellable CallbackInfoReturnable<CompactVectorArray> cir) {
        if (format.getOffset(VertexFormatElement.POSITION) == 0 && format.getVertexSize() % 4 == 0
            && polydium$decode(buffer, vertexCount, format.getVertexSize(), 0L, centroids, 0)) {
            cir.setReturnValue(centroids);
        }
        return centroids;
    }
    *///?} else {
    /*@Unique
    private static final Int2ObjectOpenHashMap<Vector3f[]> POLYDIUM_CENTROIDS = new Int2ObjectOpenHashMap<>();

    @WrapMethod(method = "unpackQuadCentroids")
    private static Vector3f[] polydium$decodeFromMemory(ByteBuffer buffer, int vertexCount, VertexFormat format, Operation<Vector3f[]> original) {
        if (format.getOffset(VertexFormatElement.POSITION) == 0 && format.getVertexSize() % 4 == 0) {
            Vector3f[] centroids = polydium$centroids(vertexCount / 4);
            if (polydium$decode(buffer, vertexCount, format.getVertexSize(), 0L, centroids, 0)) {
                return centroids;
            }
        }
        return original.call(buffer, vertexCount, format);
    }

    @Unique
    private static Vector3f[] polydium$centroids(int count) {
        boolean pooled = SortCentroidScope.discarded && RenderSystem.isOnRenderThread();
        Vector3f[] centroids = pooled ? POLYDIUM_CENTROIDS.get(count) : null;
        if (centroids == null) {
            centroids = new Vector3f[Math.max(count, 0)];
            for (int i = 0; i < count; i++) {
                centroids[i] = new Vector3f();
            }
            if (pooled) {
                if (POLYDIUM_CENTROIDS.size() >= 256) {
                    POLYDIUM_CENTROIDS.clear();
                }
                POLYDIUM_CENTROIDS.put(count, centroids);
            }
        }
        return centroids;
    }
    *///?}

    @Unique
    private static boolean polydium$decode(ByteBuffer buffer, int vertexCount, long vertexSize, long positionOffset, /*? if >= 1.21.10 {*/CompactVectorArray/*?} else {*//*Vector3f[]*//*?}*/ centroids, int firstQuad) {
        int quads = vertexCount / 4;
        if (quads <= 0 || !buffer.isDirect() || buffer.order() != ByteOrder.nativeOrder()) {
            return false;
        }
        long quadSize = vertexSize * 4L;
        long start = (long) buffer.position() + positionOffset;
        long end = start + (quads - 1) * quadSize + vertexSize * 2L + 12L;
        if (vertexSize < 0L || start < 0L || end > buffer.limit()) {
            return false;
        }

        long base = MemoryUtil.memAddress0(buffer) + start;
        for (int quad = 0; quad < quads; quad++) {
            long first = base + quad * quadSize;
            long third = first + vertexSize * 2L;
            float x0 = MemoryUtil.memGetFloat(first);
            float y0 = MemoryUtil.memGetFloat(first + 4L);
            float z0 = MemoryUtil.memGetFloat(first + 8L);
            float x2 = MemoryUtil.memGetFloat(third);
            float y2 = MemoryUtil.memGetFloat(third + 4L);
            float z2 = MemoryUtil.memGetFloat(third + 8L);
            //? if >= 1.21.10 {
            centroids.set(firstQuad + quad, (x0 + x2) / 2.0F, (y0 + y2) / 2.0F, (z0 + z2) / 2.0F);
            //?} else {
            /*centroids[firstQuad + quad].set((x0 + x2) / 2.0F, (y0 + y2) / 2.0F, (z0 + z2) / 2.0F);
            *///?}
        }
        return true;
    }
}
//?}
