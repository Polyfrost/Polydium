package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.LongBuffer;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanBindSkip {
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

    @WrapOperation(method = "setVertexBuffer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;longs(J)Ljava/nio/LongBuffer;"))
    private LongBuffer polydium$skipBindBuffers(MemoryStack stack, long value, Operation<LongBuffer> original) {
        return null;
    }

    @WrapOperation(
        method = "setVertexBuffer",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdBindVertexBuffers(Lorg/lwjgl/vulkan/VkCommandBuffer;ILjava/nio/LongBuffer;Ljava/nio/LongBuffer;)V")
    )
    private void polydium$bindVertexBufferDirectly(VkCommandBuffer commandBuffer, int slot, LongBuffer buffers, LongBuffer offsets, Operation<Void> original,
                                                   @Local MemoryStack stack, @Local(ordinal = 0) long buffer, @Local(ordinal = 1) long offset) {
        if (slot == 0) {
            if (buffer == this.polydium$boundVertexBuffer && offset == this.polydium$boundVertexOffset) {
                return;
            }
            this.polydium$boundVertexBuffer = buffer;
            this.polydium$boundVertexOffset = offset;
        }
        long values = stack.nmalloc(Long.BYTES, 2 * Long.BYTES);
        MemoryUtil.memPutLong(values, buffer);
        MemoryUtil.memPutLong(values + Long.BYTES, offset);
        VK10.nvkCmdBindVertexBuffers(commandBuffer, slot, 1, values, values + Long.BYTES);
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
