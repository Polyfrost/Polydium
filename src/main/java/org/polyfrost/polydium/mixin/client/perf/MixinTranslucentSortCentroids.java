package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.mojang.blaze3d.vertex.CompactVectorArray;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 26.2 {
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?} else {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Mixin(MeshData.class)
public abstract class MixinTranslucentSortCentroids {
    //? if >= 26.2 {
    @Inject(method = "decodeQuadCentroids", at = @At("HEAD"), cancellable = true)
    private static void polydium$decodeFromMemory(ByteBuffer buffer, int vertexCount, VertexFormat format, CompactVectorArray centroids, int firstQuad, CallbackInfo ci) {
        VertexFormatElement position = format.getElement("Position");
        if (position != null && polydium$decode(buffer, vertexCount, format.getVertexSize(), position.offset(), centroids, firstQuad)) {
            ci.cancel();
        }
    }
    //?} else {
    /*@ModifyExpressionValue(method = "unpackQuadCentroids", at = @At(value = "NEW", target = "(I)Lcom/mojang/blaze3d/vertex/CompactVectorArray;"))
    private static CompactVectorArray polydium$decodeFromMemory(CompactVectorArray centroids, @Local(argsOnly = true) ByteBuffer buffer, @Local(argsOnly = true) int vertexCount, @Local(argsOnly = true) VertexFormat format,
                                                                 @Cancellable CallbackInfoReturnable<CompactVectorArray> cir) {
        if (format.getOffset(VertexFormatElement.POSITION) == 0 && format.getVertexSize() % 4 == 0
            && polydium$decode(buffer, vertexCount, format.getVertexSize(), 0L, centroids, 0)) {
            cir.setReturnValue(centroids);
        }
        return centroids;
    }
    *///?}

    @Unique
    private static boolean polydium$decode(ByteBuffer buffer, int vertexCount, long vertexSize, long positionOffset, CompactVectorArray centroids, int firstQuad) {
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
            centroids.set(firstQuad + quad, (x0 + x2) / 2.0F, (y0 + y2) / 2.0F, (z0 + z2) / 2.0F);
        }
        return true;
    }
}
//?}
