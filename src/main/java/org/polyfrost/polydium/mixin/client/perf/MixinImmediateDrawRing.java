package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 && < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.polyfrost.polydium.client.perf.ImmediateDrawRing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.ByteBuffer;

@Mixin(RenderType.class)
public class MixinImmediateDrawRing {
    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexFormat;uploadImmediateVertexBuffer(Ljava/nio/ByteBuffer;)Lcom/mojang/blaze3d/buffers/GpuBuffer;"))
    private GpuBuffer polydium$ringVertices(VertexFormat format, ByteBuffer data, Operation<GpuBuffer> original) {
        int vertexSize = format.getVertexSize();
        int offset = ImmediateDrawRing.upload(data, vertexSize);
        if (offset < 0) {
            return original.call(format, data);
        }
        ImmediateDrawRing.setVertexOffset(offset);
        return ImmediateDrawRing.buffer();
    }

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexFormat;uploadImmediateIndexBuffer(Ljava/nio/ByteBuffer;)Lcom/mojang/blaze3d/buffers/GpuBuffer;"))
    private GpuBuffer polydium$ringIndices(VertexFormat format, ByteBuffer data, Operation<GpuBuffer> original, @Local(argsOnly = true) MeshData mesh, @Share("firstIndex") LocalIntRef firstIndex) {
        int indexSize = mesh.drawState().indexType().bytes;
        int offset = ImmediateDrawRing.upload(data, indexSize);
        if (offset < 0) {
            return original.call(format, data);
        }
        firstIndex.set(offset / indexSize);
        return ImmediateDrawRing.buffer();
    }

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIII)V"))
    private void polydium$ringOffsets(RenderPass pass, int baseVertex, int firstIndex, int indexCount, int instanceCount, Operation<Void> original, @Share("firstIndex") LocalIntRef ringFirstIndex) {
        original.call(pass, baseVertex, firstIndex + ringFirstIndex.get(), indexCount, instanceCount);
        ImmediateDrawRing.setVertexOffset(0);
    }
}
*///?}
