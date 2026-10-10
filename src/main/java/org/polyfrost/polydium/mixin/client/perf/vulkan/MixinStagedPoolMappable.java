package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.device.GpuDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

@Mixin(targets = "net.minecraft.client.renderer.StagedVertexBuffer$GpuBufferPool")
public abstract class MixinStagedPoolMappable {
    @WrapOperation(
        method = "acquire",
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuDevice;createBuffer(Ljava/util/function/Supplier;IJ)Lcom/mojang/renderpearl/api/buffers/GpuBuffer;")
    )
    private GpuBuffer polydium$createMappable(GpuDevice device, Supplier<String> label, int usage, long size, Operation<GpuBuffer> original) {
        boolean frameGeometry = usage == (GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX) || usage == (GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX);
        if (frameGeometry) {
            usage |= GpuBuffer.USAGE_MAP_WRITE;
        }
        return original.call(device, label, usage, size);
    }
}
//?}
