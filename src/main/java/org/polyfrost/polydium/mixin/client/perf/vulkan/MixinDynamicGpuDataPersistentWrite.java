package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
//? if < 26.2
/*import com.mojang.renderpearl.api.commands.CommandEncoder;*/
import com.mojang.renderpearl.backend.opengl.GlBuffer;
//? if >= 26.2
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
//? if >= 26.3 {
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
//?} else {
/*import net.minecraft.client.renderer.DynamicUniformStorage;
*///?}
import net.minecraft.client.renderer.MappableRingBuffer;
//? if >= 26.2 {
import org.polyfrost.polydium.client.perf.PersistentRingBuffer;
//?} else {
/*import org.polyfrost.polydium.client.perf.PersistentUniformView;
import org.polyfrost.polydium.mixin.client.perf.GlBufferAccessor;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.ByteBuffer;
//? if >= 26.2 {
import java.nio.ByteOrder;
import org.lwjgl.system.MemoryUtil;
import org.polyfrost.polydium.client.perf.GlSubData;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

//? if >= 26.3 {
@Mixin(DynamicGpuDataStorageMapped.class)
//?} else {
/*@Mixin(DynamicUniformStorage.class)
*///?}
public abstract class MixinDynamicGpuDataPersistentWrite {
    @Unique
    //? if >= 26.3 {
    private static final String POLYDIUM$WRITE_DATA = "writeData(Lnet/minecraft/client/renderer/DynamicGpuDataStorage$DynamicGpuData;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;";
    //?} else {
    /*private static final String POLYDIUM$WRITE_DATA = "writeUniform";
    *///?}

    //? if >= 26.3 {
    @Unique
    private static final String POLYDIUM$WRITE_DATA_ARRAY = "writeData([Lnet/minecraft/client/renderer/DynamicGpuDataStorage$DynamicGpuData;)[Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;";
    @Unique
    private static final String POLYDIUM$WRITE_DATA_BATCHED = "writeDataBatched";
    @Unique
    private static final String POLYDIUM$WRITE_DATA_BATCHED_MULTIPLE = "writeDataBatchedMultiple";
    //?} else if >= 26.2 {
    /*@Unique
    private static final String POLYDIUM$WRITE_DATA_ARRAY = "writeUniforms";
    *///?}

    //? if >= 26.2 {
    @Unique
    private static final Runnable POLYDIUM$KEEP_MAPPED = () -> {
    };

    @Shadow
    private MappableRingBuffer ringBuffer;

    @Unique
    private ByteBuffer polydium$staging;

    @Unique
    private GpuBufferSlice polydium$stagedSlice;

    @Unique
    private ByteBuffer polydium$stagedBlock;

    @Unique
    private final Runnable polydium$uploadStaged = () -> GlSubData.write(this.polydium$stagedSlice.buffer(), this.polydium$stagedSlice.offset(), this.polydium$stagedBlock.clear());
    //?} else {
    /*@Unique
    private final PersistentUniformView polydium$view = new PersistentUniformView();
    *///?}

    @Unique
    private GpuBufferSlice polydium$lastSlice;

    //? if >= 1.21.11 {
    @WrapOperation(
        method = POLYDIUM$WRITE_DATA,
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/buffers/GpuBuffer;slice(JJ)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;")
    )
    private GpuBufferSlice polydium$reuseSlice(GpuBuffer buffer, long offset, long length, Operation<GpuBufferSlice> original) {
    //?} else {
    /*@WrapOperation(
        method = POLYDIUM$WRITE_DATA,
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/buffers/GpuBuffer;slice(II)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;")
    )
    private GpuBufferSlice polydium$reuseSlice(GpuBuffer buffer, int offset, int length, Operation<GpuBufferSlice> original) {
    *///?}
        GpuBufferSlice last = this.polydium$lastSlice;
        if (last != null && last.buffer() == buffer && last.offset() == offset && last.length() == length) {
            return last;
        }
        return this.polydium$lastSlice = original.call(buffer, offset, length);
    }

    //? if >= 26.2 {
    @WrapOperation(
        //? if >= 26.3 {
        method = {POLYDIUM$WRITE_DATA, POLYDIUM$WRITE_DATA_ARRAY, POLYDIUM$WRITE_DATA_BATCHED, POLYDIUM$WRITE_DATA_BATCHED_MULTIPLE},
        //?} else {
        /*method = {POLYDIUM$WRITE_DATA, POLYDIUM$WRITE_DATA_ARRAY},
        *///?}
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;map(ZZ)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice$MappedView;")
    )
    private GpuBufferSlice.MappedView polydium$writeThroughPersistentView(GpuBufferSlice slice, boolean read, boolean write, Operation<GpuBufferSlice.MappedView> original) {
        if ((slice.buffer() instanceof VulkanGpuBuffer.Direct || slice.buffer() instanceof GlBuffer.Direct) && !slice.buffer().isClosed()) {
            ByteBuffer data = PersistentRingBuffer.persistentData(this.ringBuffer, slice.buffer());
            if (data != null) {
                ByteBuffer block = data.slice((int) slice.offset(), (int) slice.length()).order(ByteOrder.nativeOrder());
                return new GpuBufferSlice.MappedView(slice, block, POLYDIUM$KEEP_MAPPED);
            }
            if (GlSubData.applies(slice.buffer())) {
                return this.polydium$stagedView(slice);
            }
        }
        return original.call(slice, read, write);
    }

    @Unique
    private GpuBufferSlice.MappedView polydium$stagedView(GpuBufferSlice slice) {
        int length = (int) slice.length();
        ByteBuffer staging = this.polydium$staging;
        if (staging == null || staging.capacity() < length) {
            if (staging != null) {
                MemoryUtil.memFree(staging);
            }
            staging = this.polydium$staging = MemoryUtil.memAlloc(Math.max(length, 256));
        }
        this.polydium$stagedSlice = slice;
        ByteBuffer block = staging.slice(0, length).order(ByteOrder.nativeOrder());
        this.polydium$stagedBlock = block;
        return new GpuBufferSlice.MappedView(slice, block, this.polydium$uploadStaged);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void polydium$freeStaging(CallbackInfo ci) {
        if (this.polydium$staging != null) {
            MemoryUtil.memFree(this.polydium$staging);
            this.polydium$staging = null;
        }
    }
    //?} else {
    /*@WrapOperation(
        method = POLYDIUM$WRITE_DATA,
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;mapBuffer(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;ZZ)Lcom/mojang/renderpearl/api/buffers/GpuBuffer$MappedView;")
    )
    private GpuBuffer.MappedView polydium$writeThroughPersistentView(CommandEncoder encoder, GpuBufferSlice slice, boolean read, boolean write, Operation<GpuBuffer.MappedView> original) {
        if (slice.buffer() instanceof GlBuffer buffer && !buffer.isClosed() && (buffer.usage() & GpuBuffer.USAGE_MAP_WRITE) != 0) {
            ByteBuffer data = ((GlBufferAccessor) buffer).polydium$persistentBuffer();
            if (data != null) {
                return this.polydium$view.over(buffer, data, slice);
            }
        }
        return original.call(encoder, slice, read, write);
    }
    *///?}
}
//?}
