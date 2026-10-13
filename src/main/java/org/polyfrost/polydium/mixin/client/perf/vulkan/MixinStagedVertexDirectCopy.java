package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.CompactVectorArray;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import net.minecraft.client.renderer.StagedVertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;
import java.util.List;

@Mixin(StagedVertexBuffer.class)
public abstract class MixinStagedVertexDirectCopy {
    @Shadow
    private static CompactVectorArray decodeSortingPoints(StagedVertexBuffer.Draw draw) {
        throw new AssertionError();
    }

    @WrapMethod(method = "uploadDrawsToBuffers")
    private void polydium$writeDirectly(GpuDevice device, List<StagedVertexBuffer.Draw> draws, GpuBuffer vertexGpuBuffer, GpuBuffer indexGpuBuffer, int vertexBufferSize, int indexBufferSize, Operation<Void> original) {
        if (!polydium$isWritable(vertexGpuBuffer)
            || (indexGpuBuffer != null && !polydium$isWritable(indexGpuBuffer))) {
            original.call(device, draws, vertexGpuBuffer, indexGpuBuffer, vertexBufferSize, indexBufferSize);
            return;
        }
        try (GpuBufferSlice.MappedView view = vertexGpuBuffer.slice(0L, vertexBufferSize).map(false, true)) {
            ByteBuffer buffer = view.data();
            for (StagedVertexBuffer.Draw draw : draws) {
                if (!draw.isEmpty()) {
                    MixinStagedDrawAccessor accessor = (MixinStagedDrawAccessor) draw;
                    buffer.position(accessor.polydium$vertexOffset());
                    for (ByteBufferBuilder.Result slice : accessor.polydium$vertexBufferSlices()) {
                        buffer.put(slice.byteBuffer());
                    }
                }
            }
        }
        try (GpuBufferSlice.MappedView view = indexGpuBuffer == null ? null : indexGpuBuffer.slice(0L, indexBufferSize).map(false, true)) {
            for (StagedVertexBuffer.Draw draw : draws) {
                if (!draw.isEmpty()) {
                    MixinStagedDrawAccessor accessor = (MixinStagedDrawAccessor) draw;
                    VertexSorting sorting = accessor.polydium$quadSorting();
                    if (view != null && sorting != null) {
                        MeshData.SortState sortState = new MeshData.SortState(decodeSortingPoints(draw), accessor.polydium$indexType());
                        ByteBuffer buffer = view.data();
                        buffer.position(accessor.polydium$indexOffset());
                        sortState.writeSortedIndexBuffer(buffer, sorting);
                    }
                    accessor.polydium$freeVertexData();
                }
            }
        }
    }

    @Unique
    private static boolean polydium$isWritable(GpuBuffer buffer) {
        return (buffer instanceof VulkanGpuBuffer.Direct || buffer instanceof GlBuffer.Direct) && (buffer.usage() & GpuBuffer.USAGE_MAP_WRITE) != 0;
    }
}
//?}
