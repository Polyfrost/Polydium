package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanGpuSurface.class)
public abstract class MixinVulkanAcquireTimeout {
    @Shadow
    @Final
    private VulkanDevice device;

    @ModifyExpressionValue(method = "acquireNextTexture", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRSwapchain;vkAcquireNextImageKHR(Lorg/lwjgl/vulkan/VkDevice;JJJJLjava/nio/IntBuffer;)I", remap = false))
    private int polydium$recreateOnTimeout(int result) {
        if (result != VK10.VK_TIMEOUT) {
            return result;
        }
        VulkanSubmitWaitAccessor encoder = (VulkanSubmitWaitAccessor) this.device.createCommandEncoder();
        return encoder.polydium$awaitSubmitCompletion(encoder.polydium$currentSubmitIndex() - 1L, 5_000_000_000L)
            ? KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR
            : result;
    }
}
//?}
