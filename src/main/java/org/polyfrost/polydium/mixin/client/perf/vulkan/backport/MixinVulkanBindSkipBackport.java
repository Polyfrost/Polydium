package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.LongBuffer;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanBindSkipBackport {
    @Unique
    private final VulkanDynamicState.Bindings polydium$bindings = new VulkanDynamicState.Bindings();

    @WrapWithCondition(
        method = "setPipeline",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdBindPipeline(Lorg/lwjgl/vulkan/VkCommandBuffer;IJ)V")
    )
    private boolean polydium$bindChangedPipeline(VkCommandBuffer commandBuffer, int bindPoint, long pipeline) {
        return VulkanDynamicState.changesPipeline(commandBuffer.address(), pipeline);
    }

    @WrapWithCondition(
        method = "setVertexBuffer",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdBindVertexBuffers(Lorg/lwjgl/vulkan/VkCommandBuffer;ILjava/nio/LongBuffer;Ljava/nio/LongBuffer;)V")
    )
    private boolean polydium$bindChangedVertexBuffer(VkCommandBuffer commandBuffer, int slot, LongBuffer buffers, LongBuffer offsets) {
        return this.polydium$bindings.changesVertexBuffer(slot, buffers.get(buffers.position()), offsets.get(offsets.position()));
    }

    @WrapWithCondition(
        method = "setIndexBuffer",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdBindIndexBuffer(Lorg/lwjgl/vulkan/VkCommandBuffer;JJI)V")
    )
    private boolean polydium$bindChangedIndexBuffer(VkCommandBuffer commandBuffer, long buffer, long offset, int indexType) {
        return this.polydium$bindings.changesIndexBuffer(buffer, offset, indexType);
    }
}
