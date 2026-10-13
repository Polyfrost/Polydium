package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.device.SurfaceException;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface;
import com.mojang.renderpearl.backend.vulkan.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.polyfrost.polydium.client.perf.PresentThread;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VulkanGpuSurface.class)
public abstract class MixinVulkanGpuSurfacePresentThread {

    @Shadow
    @Final
    private VulkanDevice device;

    @Shadow
    @Final
    private VkQueue presentQueue;

    @Shadow
    private long swapchain;

    @Shadow
    private long[] presentSemaphores;

    @Shadow
    private int currentImageIndex;

    @Shadow
    private SurfaceException eatenException;

    @Shadow
    private boolean swapchainSuboptimal;

    @Shadow
    private boolean swapchainOutOfDate;

    @Inject(method = "close", at = @At("HEAD"))
    private void polydium$stopPresentThread(CallbackInfo ci) {
        PresentThread.vulkanSurface = false;
        PresentThread.stop();
    }

    @Inject(method = "configure", at = @At("HEAD"))
    private void polydium$drainBeforeConfigure(CallbackInfo ci) {
        PresentThread.vulkanSurface = true;
        PresentThread.drain();
    }

    @Inject(method = "acquireNextTexture", at = @At("HEAD"))
    private void polydium$drainPresentThread(CallbackInfo ci) {
        PresentThread.drain();
    }

    @Inject(method = "isSuboptimal", at = @At("HEAD"))
    private void polydium$drainBeforeSuboptimal(CallbackInfoReturnable<Boolean> cir) {
        PresentThread.drain();
    }

    @ModifyArg(method = "configure", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkSwapchainCreateInfoKHR;minImageCount(I)Lorg/lwjgl/vulkan/VkSwapchainCreateInfoKHR;"))
    private int polydium$spareImmediateImage(int imageCount, @Local(argsOnly = true) GpuSurface.Configuration config, @Local VkSurfaceCapabilitiesKHR capabilities) {
        if (config.presentMode() != GpuSurface.PresentMode.IMMEDIATE)
            return imageCount;

        int maxImageCount = capabilities.maxImageCount();

        if (maxImageCount != 0 && imageCount + 1 > maxImageCount)
            return imageCount;

        return imageCount + 1;
    }

    @WrapMethod(method = "present")
    private void polydium$handOffPresent(Operation<Void> original) {
        if (!PresentThread.isCapturing()) {
            PresentThread.drain();
            original.call();

            return;
        }

        if (this.swapchainOutOfDate)
            throw new IllegalStateException("Attempt to use out of date swapchain");

        long semaphore  = this.presentSemaphores[this.currentImageIndex];
        long frameChain = this.swapchain;
        int  imageIndex = this.currentImageIndex;

        this.currentImageIndex = -1;
        PresentThread.capture(() -> this.polydium$queuePresent(semaphore, frameChain, imageIndex));
    }

    @Unique
    private void polydium$queuePresent(long semaphore, long frameChain, int imageIndex) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack).sType$Default();

            presentInfo.pWaitSemaphores(stack.longs(semaphore));
            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(stack.longs(frameChain));
            presentInfo.pImageIndices(stack.ints(imageIndex));

            int result = KHRSwapchain.vkQueuePresentKHR(this.presentQueue, presentInfo);

            if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                this.swapchainSuboptimal = true;
                this.swapchainOutOfDate  = true;
                this.eatenException      = new SurfaceException("Failed to present image, swapchain out of date");
            } else if (result == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                this.swapchainSuboptimal = true;
            } else {
                VulkanUtils.crashIfFailure(this.device, result, "Failed to present image");
            }
        }
    }
}
//?}
