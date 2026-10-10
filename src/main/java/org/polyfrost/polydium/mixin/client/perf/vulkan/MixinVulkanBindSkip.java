package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.LongBuffer;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanBindSkip {
    @Shadow
    @Final
    private VkCommandBuffer commandBuffer;

    @Unique
    private long polydium$boundPipeline;
    @Unique
    private long polydium$boundVertexBuffer;
    @Unique
    private long polydium$boundVertexOffset;
    @Unique
    private long polydium$boundIndexBuffer;
    @Unique
    private long polydium$boundIndexOffset;
    @Unique
    private int polydium$boundIndexType;

    @WrapWithCondition(
        method = "setPipeline",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdBindPipeline(Lorg/lwjgl/vulkan/VkCommandBuffer;IJ)V")
    )
    private boolean polydium$bindChangedPipeline(VkCommandBuffer commandBuffer, int bindPoint, long pipeline) {
        this.polydium$boundPipeline = pipeline;
        return VulkanDynamicState.changesPipeline(commandBuffer.address(), pipeline);
    }

    @Inject(method = "setVertexBuffer", at = @At("HEAD"), cancellable = true)
    private void polydium$bindVertexBufferDirectly(int slot, GpuBufferSlice vertexBuffer, CallbackInfo ci) {
        if (vertexBuffer == null) {
            return;
        }
        long buffer = ((VulkanGpuBuffer) vertexBuffer.buffer()).vkBuffer();
        long offset = vertexBuffer.offset();
        if (slot == 0) {
            if (buffer == this.polydium$boundVertexBuffer && offset == this.polydium$boundVertexOffset) {
                ci.cancel();
                return;
            } else {
                this.polydium$boundVertexBuffer = buffer;
                this.polydium$boundVertexOffset = offset;
            }
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long values = stack.nmalloc(Long.BYTES, 2 * Long.BYTES);
            MemoryUtil.memPutLong(values, buffer);
            MemoryUtil.memPutLong(values + Long.BYTES, offset);
            VK10.nvkCmdBindVertexBuffers(this.commandBuffer, slot, 1, values, values + Long.BYTES);
        }
        ci.cancel();
    }

    @WrapWithCondition(
        method = "setVertexBuffer",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdBindVertexBuffers(Lorg/lwjgl/vulkan/VkCommandBuffer;ILjava/nio/LongBuffer;Ljava/nio/LongBuffer;)V")
    )
    private boolean polydium$bindChangedVertexBuffer(VkCommandBuffer commandBuffer, int slot, LongBuffer buffers, LongBuffer offsets) {
        if (slot != 0) {
            return true;
        }
        if (buffers.remaining() != 1 || offsets.remaining() != 1) {
            this.polydium$boundVertexBuffer = 0L;
            return true;
        }
        long buffer = buffers.get(buffers.position());
        long offset = offsets.get(offsets.position());
        if (buffer == this.polydium$boundVertexBuffer && offset == this.polydium$boundVertexOffset) {
            return false;
        }
        this.polydium$boundVertexBuffer = buffer;
        this.polydium$boundVertexOffset = offset;
        return true;
    }

    @WrapWithCondition(
        method = "setIndexBuffer",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdBindIndexBuffer(Lorg/lwjgl/vulkan/VkCommandBuffer;JJI)V")
    )
    private boolean polydium$bindChangedIndexBuffer(VkCommandBuffer commandBuffer, long buffer, long offset, int indexType) {
        if (buffer == this.polydium$boundIndexBuffer && offset == this.polydium$boundIndexOffset && indexType == this.polydium$boundIndexType) {
            return false;
        }
        this.polydium$boundIndexBuffer = buffer;
        this.polydium$boundIndexOffset = offset;
        this.polydium$boundIndexType = indexType;
        return true;
    }
}
//?}
