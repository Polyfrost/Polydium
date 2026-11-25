package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanQueue;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo2;
import org.polyfrost.polydium.client.perf.VulkanSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanQueue.Submission.class)
public abstract class MixinVulkanLegacySubmission {
    @WrapOperation(
        method = "close",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRSynchronization2;vkQueueSubmit2KHR(Lorg/lwjgl/vulkan/VkQueue;Lorg/lwjgl/vulkan/VkSubmitInfo2$Buffer;J)I")
    )
    private int polydium$legacySubmit(VkQueue queue, VkSubmitInfo2.Buffer submits, long fence, Operation<Integer> original) {
        return VulkanSync.sync2 ? original.call(queue, submits, fence) : VulkanSync.queueSubmit(queue, submits, fence);
    }
}
//?}
