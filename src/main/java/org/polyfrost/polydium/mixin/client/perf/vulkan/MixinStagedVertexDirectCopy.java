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
import org.lwjgl.system.MemoryUtil;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.VertexCopy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Mixin(StagedVertexBuffer.class)
public abstract class MixinStagedVertexDirectCopy implements VertexCopy.Owner {
    @Unique
    private final VertexCopy polydium$vertexCopy = new VertexCopy();

    @Unique
    private final List<StagedVertexBuffer.Draw> polydium$copiedDraws = new ArrayList<>();

    @Unique
    private GpuBufferSlice.MappedView polydium$vertexView;

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
        if (PolydiumToggles.backgroundVertexCopy && VertexCopy.isLevelPrepare() && vertexGpuBuffer instanceof VulkanGpuBuffer.Direct) {
            this.polydium$queueCopies(draws, vertexGpuBuffer, indexGpuBuffer, vertexBufferSize, indexBufferSize);
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
    private void polydium$queueCopies(List<StagedVertexBuffer.Draw> draws, GpuBuffer vertexGpuBuffer, GpuBuffer indexGpuBuffer, int vertexBufferSize, int indexBufferSize) {
        if (this.polydium$vertexView != null)
            throw new IllegalStateException("The previous vertex upload has not finished");

        VertexCopy.track(this);
        this.polydium$vertexView = vertexGpuBuffer.slice(0L, vertexBufferSize).map(false, true);

        try {
            ByteBuffer vertices = this.polydium$vertexView.data();

            this.polydium$vertexCopy.begin();

            for (StagedVertexBuffer.Draw draw : draws) {
                if (draw.isEmpty())
                    continue;

                MixinStagedDrawAccessor accessor = (MixinStagedDrawAccessor) draw;

                this.polydium$copiedDraws.add(draw);
                vertices.position(accessor.polydium$vertexOffset());

                for (ByteBufferBuilder.Result slice : accessor.polydium$vertexBufferSlices())
                    this.polydium$queueCopy(slice, vertices);
            }

            this.polydium$vertexCopy.seal();

            if (indexGpuBuffer == null)
                return;

            try (GpuBufferSlice.MappedView view = indexGpuBuffer.slice(0L, indexBufferSize).map(false, true)) {
                ByteBuffer buffer = view.data();

                for (StagedVertexBuffer.Draw draw : draws) {
                    MixinStagedDrawAccessor accessor = (MixinStagedDrawAccessor) draw;

                    if (draw.isEmpty() || accessor.polydium$quadSorting() == null)
                        continue;

                    MeshData.SortState sortState = new MeshData.SortState(decodeSortingPoints(draw), accessor.polydium$indexType());

                    buffer.position(accessor.polydium$indexOffset());
                    sortState.writeSortedIndexBuffer(buffer, accessor.polydium$quadSorting());
                }
            }
        } catch (Throwable thrown) {
            try {
                this.polydium$finishVertexCopy();
            } catch (Throwable suppressed) {
                thrown.addSuppressed(suppressed);
            }

            throw thrown;
        }
    }

    @Unique
    private void polydium$queueCopy(ByteBufferBuilder.Result slice, ByteBuffer buffer) {
        int size  = slice.size();
        int start = buffer.position();

        if (size > buffer.remaining())
            throw new BufferOverflowException();

        this.polydium$vertexCopy.add(MemoryUtil.memAddress(slice.byteBuffer()), MemoryUtil.memAddress(buffer), size);
        buffer.position(start + size);
    }

    @ModifyVariable(method = "getVertexBuilder", at = @At("HEAD"), argsOnly = true)
    private StagedVertexBuffer.Draw polydium$finishBeforeReuse(StagedVertexBuffer.Draw draw) {
        this.polydium$finishVertexCopy();

        return draw;
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void polydium$finishBeforeClose(CallbackInfo ci) {
        this.polydium$finishVertexCopy();
    }

    @Override
    public void polydium$finishVertexCopy() {
        if (this.polydium$vertexView == null)
            return;

        try {
            this.polydium$vertexCopy.finish();
        } finally {
            GpuBufferSlice.MappedView view = this.polydium$vertexView;

            this.polydium$vertexView = null;
            VertexCopy.untrack(this);

            try {
                view.close();
            } finally {
                for (StagedVertexBuffer.Draw draw : this.polydium$copiedDraws)
                    ((MixinStagedDrawAccessor) draw).polydium$freeVertexData();

                this.polydium$copiedDraws.clear();
            }
        }
    }

    @Unique
    private static boolean polydium$isWritable(GpuBuffer buffer) {
        return (buffer instanceof VulkanGpuBuffer.Direct || buffer instanceof GlBuffer.Direct) && (buffer.usage() & GpuBuffer.USAGE_MAP_WRITE) != 0;
    }
}
//?}
