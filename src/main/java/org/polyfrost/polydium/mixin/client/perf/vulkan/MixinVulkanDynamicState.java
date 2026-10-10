package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanDynamicState {
    @WrapWithCondition(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdSetViewport(Lorg/lwjgl/vulkan/VkCommandBuffer;ILorg/lwjgl/vulkan/VkViewport$Buffer;)V")
    )
    private boolean polydium$setChangedViewport(VkCommandBuffer commandBuffer, int firstViewport, VkViewport.Buffer viewports) {
        if (firstViewport != 0 || viewports.remaining() != 1) {
            VulkanDynamicState.reset();
            return true;
        }
        VkViewport viewport = viewports.get(viewports.position());
        return VulkanDynamicState.changesViewport(commandBuffer.address(), viewport.x(), viewport.y(), viewport.width(),
            viewport.height(), viewport.minDepth(), viewport.maxDepth());
    }

    @WrapWithCondition(
        method = "setScissor",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdSetScissor(Lorg/lwjgl/vulkan/VkCommandBuffer;ILorg/lwjgl/vulkan/VkRect2D$Buffer;)V")
    )
    private static boolean polydium$setChangedScissor(VkCommandBuffer commandBuffer, int firstScissor, VkRect2D.Buffer scissors) {
        if (firstScissor != 0 || scissors.remaining() != 1) {
            VulkanDynamicState.reset();
            return true;
        }
        VkRect2D scissor = scissors.get(scissors.position());
        return VulkanDynamicState.changesScissor(commandBuffer.address(), scissor.offset().x(), scissor.offset().y(),
            scissor.extent().width(), scissor.extent().height());
    }
}
//?}
