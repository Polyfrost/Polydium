package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.IntBuffer;

@Mixin(VulkanGpuSurface.class)
public abstract class MixinGpuProfileVulkanSurface {
    @WrapOperation(method = "blitFromTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;allocateAndBeginTransientCommandBuffer()Lorg/lwjgl/vulkan/VkCommandBuffer;"))
    private VkCommandBuffer polydium$profileBlitStart(VulkanCommandEncoder encoder, Operation<VkCommandBuffer> original) {
        if (!GpuProfiler.recording)
            return original.call(encoder);

        ((VulkanCommandEncoderInvoker) encoder).polydium$endCommandBuffer();

        VkCommandBuffer buffer = original.call(encoder);

        GpuProfiler.beginOp(buffer, GpuProfiler.OP_PRESENT);

        return buffer;
    }

    @WrapOperation(method = "blitFromTexture", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkEndCommandBuffer(Lorg/lwjgl/vulkan/VkCommandBuffer;)I", remap = false))
    private int polydium$profileBlitEnd(VkCommandBuffer buffer, Operation<Integer> original) {
        if (GpuProfiler.recording)
            GpuProfiler.end(buffer);

        return original.call(buffer);
    }

    @WrapOperation(method = "acquireNextTexture", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRSwapchain;vkAcquireNextImageKHR(Lorg/lwjgl/vulkan/VkDevice;JJJJLjava/nio/IntBuffer;)I", remap = false))
    private int polydium$timeAcquire(VkDevice device, long swapchain, long timeout, long semaphore, long fence, IntBuffer index, Operation<Integer> original) {
        long start  = GpuProfiler.presentStart();
        int  result = original.call(device, swapchain, timeout, semaphore, fence, index);

        GpuProfiler.presentWait(start, false);

        return result;
    }

    @WrapOperation(method = "present", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRSwapchain;vkQueuePresentKHR(Lorg/lwjgl/vulkan/VkQueue;Lorg/lwjgl/vulkan/VkPresentInfoKHR;)I", remap = false))
    private int polydium$timePresent(VkQueue queue, VkPresentInfoKHR info, Operation<Integer> original) {
        long start  = GpuProfiler.presentStart();
        int  result = original.call(queue, info);

        GpuProfiler.presentWait(start, true);

        return result;
    }
}
//?}
