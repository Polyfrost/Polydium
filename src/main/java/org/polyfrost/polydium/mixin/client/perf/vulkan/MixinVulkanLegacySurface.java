package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.polyfrost.polydium.client.perf.VulkanSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanGpuSurface.class)
public abstract class MixinVulkanLegacySurface {
    @WrapOperation(
        method = "blitFromTexture",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRSynchronization2;vkCmdPipelineBarrier2KHR(Lorg/lwjgl/vulkan/VkCommandBuffer;Lorg/lwjgl/vulkan/VkDependencyInfo;)V"),
        require = 2
    )
    private void polydium$legacyBarrier(VkCommandBuffer commandBuffer, VkDependencyInfo info, Operation<Void> original) {
        if (VulkanSync.sync2) {
            original.call(commandBuffer, info);
        } else {
            VulkanSync.pipelineBarrier(commandBuffer, info);
        }
    }
}
//?}
