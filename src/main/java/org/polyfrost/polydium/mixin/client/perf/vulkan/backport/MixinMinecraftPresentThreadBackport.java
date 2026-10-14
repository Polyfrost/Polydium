package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.polyfrost.polydium.client.perf.FramePath;
import org.polyfrost.polydium.client.perf.PresentThread;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.compat.GpuSurface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanDevice.class)
public abstract class MixinMinecraftPresentThreadBackport {
    @Shadow
    private GpuSurface surface;

    @WrapOperation(method = "presentTexture", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/compat/GpuSurface;acquireNextTexture()V"))
    private void polydium$measureAcquire(GpuSurface surface, Operation<Void> original) {
        long acquireStart = System.nanoTime();
        try {
            original.call(surface);
        } finally {
            if (FramePath.frameUsesWorker())
                FramePath.recordAcquire(System.nanoTime() - acquireStart);
        }
    }

    @WrapOperation(method = "presentFrame", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanCommandEncoder;submit()V"))
    private void polydium$presentOnPresentThread(VulkanCommandEncoder encoder, Operation<Void> original) {
        FramePath.beginFrame(PresentThread.canDeferAcquire(this.surface));
        if (this.surface.isAcquired())
            PresentThread.arm(this.surface);
        try {
            original.call(encoder);
        } finally {
            PresentThread.disarm();
        }
        FramePath.frameSubmitted();
    }

    @WrapOperation(method = "presentFrame", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/compat/GpuSurface;isAcquired()Z"))
    private boolean polydium$keepPresentCallSite(GpuSurface surface, Operation<Boolean> original) {
        return PresentThread.isHandedOff() || original.call(surface);
    }

    @WrapOperation(method = "presentFrame", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/compat/GpuSurface;present()V"))
    private void polydium$skipHandedOffPresent(GpuSurface surface, Operation<Void> original) {
        if (!PresentThread.takeHandedOff())
            original.call(surface);
    }
}
