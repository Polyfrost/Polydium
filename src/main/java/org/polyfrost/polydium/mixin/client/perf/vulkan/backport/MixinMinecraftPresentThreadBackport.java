package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.FramePath;
import org.polyfrost.polydium.client.perf.PresentThread;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.compat.GpuSurface;
import org.polyfrost.polydium.vulkan.compat.SurfaceException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanDevice.class)
public abstract class MixinMinecraftPresentThreadBackport {
    @Shadow
    private GpuSurface surface;

    @Shadow
    private boolean surfaceNeedsReconfiguring;

    @Unique
    private boolean polydium$imageAcquiredEarly;

    @Unique
    private SurfaceException polydium$earlyAcquireFailure;

    @WrapOperation(method = "presentTexture", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/compat/GpuSurface;configure(Lorg/polyfrost/polydium/vulkan/compat/GpuSurface$Configuration;)V"))
    private void polydium$dropEarlyBeforeConfigure(GpuSurface surface, GpuSurface.Configuration config, Operation<Void> original) {
        this.polydium$dropEarlyAcquire(surface);
        original.call(surface, config);
    }

    @WrapOperation(method = "close", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/compat/GpuSurface;close()V"))
    private void polydium$dropEarlyBeforeClose(GpuSurface surface, Operation<Void> original) {
        this.polydium$dropEarlyAcquire(surface);
        original.call(surface);
    }

    @Inject(method = "presentFrame", at = @At("TAIL"))
    private void polydium$acquireNextEarly(CallbackInfo ci) {
        if (!PolydiumToggles.earlyAcquire || this.polydium$imageAcquiredEarly || this.polydium$earlyAcquireFailure != null)
            return;

        if (!FramePath.nextFrameAcquiresEarly(PresentThread.canDeferAcquire(this.surface)))
            return;

        if (this.surface.isAcquired() || this.surfaceNeedsReconfiguring || this.surface.isSuboptimal())
            return;

        try {
            this.surface.acquireNextTexture();
            this.polydium$imageAcquiredEarly = true;
        } catch (SurfaceException exception) {
            this.polydium$earlyAcquireFailure = exception;
        }
    }

    @Unique
    private void polydium$dropEarlyAcquire(GpuSurface surface) {
        if (this.polydium$imageAcquiredEarly)
            ((PresentThread.Discard) surface).polydium$discardAcquired();

        this.polydium$imageAcquiredEarly  = false;
        this.polydium$earlyAcquireFailure = null;
    }

    @WrapOperation(method = "presentTexture", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/compat/GpuSurface;acquireNextTexture()V"))
    private void polydium$measureAcquire(GpuSurface surface, Operation<Void> original) throws SurfaceException {
        if (this.polydium$imageAcquiredEarly || this.polydium$earlyAcquireFailure != null) {
            SurfaceException failure = this.polydium$earlyAcquireFailure;

            this.polydium$imageAcquiredEarly  = false;
            this.polydium$earlyAcquireFailure = null;

            if (failure != null)
                throw failure;

            return;
        }

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
        if (this.surface.isAcquired() && !this.polydium$imageAcquiredEarly)
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
        return PresentThread.isHandedOff() || !this.polydium$imageAcquiredEarly && original.call(surface);
    }

    @WrapOperation(method = "presentFrame", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/compat/GpuSurface;present()V"))
    private void polydium$skipHandedOffPresent(GpuSurface surface, Operation<Void> original) {
        if (!PresentThread.takeHandedOff())
            original.call(surface);
    }
}
