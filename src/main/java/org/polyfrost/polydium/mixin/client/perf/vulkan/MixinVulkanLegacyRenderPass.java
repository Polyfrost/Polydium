package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanLegacyRenderPass {
    @WrapOperation(
        method = "writeTimestamp",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRSynchronization2;vkCmdWriteTimestamp2KHR(Lorg/lwjgl/vulkan/VkCommandBuffer;JJI)V")
    )
    private void polydium$legacyTimestamp(VkCommandBuffer commandBuffer, long stage, long queryPool, int query, Operation<Void> original) {
        if (VulkanSync.sync2) {
            original.call(commandBuffer, stage, queryPool, query);
        } else {
            VulkanSync.writeTimestamp(commandBuffer, stage, queryPool, query);
        }
    }
}
//?}
