package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanDynamicStateBackport {
    @WrapWithCondition(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdSetViewport(Lorg/lwjgl/vulkan/VkCommandBuffer;ILorg/lwjgl/vulkan/VkViewport$Buffer;)V")
    )
    private boolean polydium$setChangedViewport(VkCommandBuffer commandBuffer, int firstViewport, VkViewport.Buffer viewports) {
        return VulkanDynamicState.changesViewport(commandBuffer, firstViewport, viewports);
    }

    @WrapWithCondition(
        method = "setScissor",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdSetScissor(Lorg/lwjgl/vulkan/VkCommandBuffer;ILorg/lwjgl/vulkan/VkRect2D$Buffer;)V")
    )
    private static boolean polydium$setChangedScissor(VkCommandBuffer commandBuffer, int firstScissor, VkRect2D.Buffer scissors) {
        return VulkanDynamicState.changesScissor(commandBuffer, firstScissor, scissors);
    }
}
