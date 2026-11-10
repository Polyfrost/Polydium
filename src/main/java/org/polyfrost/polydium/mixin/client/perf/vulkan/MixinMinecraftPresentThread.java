package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.device.SurfaceException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
//? if < 26.3
/*import org.lwjgl.glfw.GLFW;*/
import org.objectweb.asm.Opcodes;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.FramePath;
import org.polyfrost.polydium.client.perf.PresentThread;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftPresentThread {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private Window window;

    @Shadow
    @Final
    private GpuSurface windowSurface;

    @Shadow
    @Final
    public Options options;

    @Shadow
    private boolean windowSurfaceNeedsReconfiguring;

    @Shadow
    private boolean surfaceIsInvalid;

    @Unique
    private boolean polydium$presentThread;

    @Unique
    private boolean polydium$acquireDeferred;

    @Unique
    private boolean polydium$imageAcquiredEarly;

    @Unique
    private SurfaceException polydium$earlyAcquireFailure;

    @Unique
    private boolean polydium$frameAcquiredEarly;

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;isAcquired()Z", ordinal = 0))
    private boolean polydium$startWithEarlyImage(GpuSurface surface, Operation<Boolean> original) {
        if (this.polydium$imageAcquiredEarly)
            return false;

        return original.call(surface);
    }

    @ModifyExpressionValue(method = "renderFrame", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;windowSurfaceNeedsReconfiguring:Z", opcode = Opcodes.GETFIELD))
    private boolean polydium$deferAcquire(boolean needsReconfiguring) {
        if (!this.polydium$presentThread) {
            this.polydium$presentThread          = true;
            this.windowSurfaceNeedsReconfiguring = true;
            needsReconfiguring                   = true;
            FramePath.reset();
        }

        boolean worker = FramePath.beginFrame(PresentThread.canDeferAcquire(this.windowSurface));

        this.polydium$frameAcquiredEarly = this.polydium$imageAcquiredEarly || this.polydium$earlyAcquireFailure != null;
        this.polydium$acquireDeferred    = !needsReconfiguring && worker && !this.polydium$frameAcquiredEarly;

        return needsReconfiguring;
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;isSuboptimal()Z"))
    private boolean polydium$skipDeferredSuboptimal(GpuSurface surface, Operation<Boolean> original) {
        if (this.polydium$acquireDeferred || this.polydium$frameAcquiredEarly)
            return false;

        return original.call(surface);
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;configure(Lcom/mojang/renderpearl/api/device/GpuSurface$Configuration;)V"))
    private void polydium$dropEarlyBeforeConfigure(GpuSurface surface, GpuSurface.Configuration config, Operation<Void> original) {
        this.polydium$dropEarlyAcquire(surface);
        original.call(surface, config);
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;acquireNextTexture()V"))
    private void polydium$useEarlyAcquire(GpuSurface surface, Operation<Void> original) throws SurfaceException {
        if (!this.polydium$frameAcquiredEarly) {
            original.call(surface);

            return;
        }

        SurfaceException failure = this.polydium$earlyAcquireFailure;

        this.polydium$frameAcquiredEarly  = false;
        this.polydium$imageAcquiredEarly  = false;
        this.polydium$earlyAcquireFailure = null;

        if (failure != null)
            throw failure;
    }

    //? if < 26.3 {
    /*@ModifyExpressionValue(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;isMinimized()Z"))
    private boolean polydium$consumeEarlyWhenMinimized(boolean minimized) {
        // Minimizing after the early acquire would otherwise skip the acquire call that consumes its image
        return minimized && !this.polydium$frameAcquiredEarly;
    }
    *///?}

    @ModifyExpressionValue(method = "renderFrame", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;surfaceIsInvalid:Z", opcode = Opcodes.GETFIELD))
    private boolean polydium$skipDeferredAcquire(boolean invalid) {
        return invalid || this.polydium$acquireDeferred;
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;isAcquired()Z", ordinal = 1))
    private boolean polydium$acquireBeforeBlit(GpuSurface surface, Operation<Boolean> original) {
        if (this.polydium$acquireDeferred) {
            this.polydium$acquireDeferred = false;
            this.polydium$configureAndAcquire();
        }

        return original.call(surface);
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;submit()V"))
    private void polydium$presentOnPresentThread(CommandEncoder encoder, Operation<Void> original) {
        if (!this.polydium$presentThread) {
            original.call(encoder);

            return;
        }

        if (this.windowSurface.isAcquired())
            PresentThread.arm(this.windowSurface);

        try {
            original.call(encoder);
        } finally {
            PresentThread.disarm();
        }

        FramePath.frameSubmitted();
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;isAcquired()Z", ordinal = 2))
    private boolean polydium$keepPresentCallSite(GpuSurface surface, Operation<Boolean> original) {
        return PresentThread.isHandedOff() || original.call(surface);
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;present()V"))
    private void polydium$skipHandedOffPresent(GpuSurface surface, Operation<Void> original) {
        if (!PresentThread.takeHandedOff())
            original.call(surface);
    }

    @Inject(method = "renderFrame", at = @At("TAIL"))
    private void polydium$acquireNextEarly(boolean advanceGameTime, CallbackInfo ci) {
        if (this.polydium$frameAcquiredEarly)
            throw new IllegalStateException("The screen image acquired early was not used by its frame");

        if (!PolydiumToggles.earlyAcquire || !this.polydium$presentThread || !FramePath.nextFrameAcquiresEarly(PresentThread.canDeferAcquire(this.windowSurface)))
            return;

        if (this.windowSurface.isAcquired() || this.surfaceIsInvalid || this.windowSurfaceNeedsReconfiguring || this.windowSurface.isSuboptimal())
            return;
        //? if < 26.3 {
        /*if (this.window.isMinimized())
            return;
        *///?}

        try {
            this.windowSurface.acquireNextTexture();
            this.polydium$imageAcquiredEarly = true;
        } catch (SurfaceException exception) {
            this.polydium$earlyAcquireFailure = exception;
        }
    }

    @WrapOperation(method = "close", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;close()V"))
    private void polydium$dropEarlyBeforeClose(GpuSurface surface, Operation<Void> original) {
        this.polydium$dropEarlyAcquire(surface);
        original.call(surface);
    }

    @Unique
    private void polydium$dropEarlyAcquire(GpuSurface surface) {
        if (this.polydium$imageAcquiredEarly)
            ((PresentThread.Discard) surface).polydium$discardAcquired();

        this.polydium$imageAcquiredEarly  = false;
        this.polydium$earlyAcquireFailure = null;
        this.polydium$frameAcquiredEarly  = false;
    }

    @Unique
    private void polydium$configureAndAcquire() {
        //? if >= 26.3 {
        if ((this.windowSurfaceNeedsReconfiguring || this.windowSurface.isSuboptimal() && !this.surfaceIsInvalid) && !this.window.isIconified()) {
            Window.FramebufferSize   framebufferSize = this.window.queryFramebufferSize();
            GpuSurface.PresentMode   presentMode     = GpuSurface.PresentMode.getSupportedVsyncMode(this.windowSurface.supportedPresentModes(), this.options.enableVsync().get());
            GpuSurface.Configuration config          = new GpuSurface.Configuration(framebufferSize.width(), framebufferSize.height(), presentMode);

            try {
                this.windowSurface.configure(config);
                this.surfaceIsInvalid                = false;
                this.windowSurfaceNeedsReconfiguring = false;
            } catch (SurfaceException exception) {
                LOGGER.warn("Couldn't configure surface to {}", config, exception);
                this.surfaceIsInvalid = true;
            }
        }

        if (this.surfaceIsInvalid)
            return;
        //?} else {
        /*if (this.windowSurfaceNeedsReconfiguring || this.windowSurface.isSuboptimal() && !this.surfaceIsInvalid) {
            int[] width  = new int[1];
            int[] height = new int[1];

            GLFW.glfwGetFramebufferSize(this.window.handle(), width, height);

            if (width[0] != 0 || height[0] != 0) {
                GpuSurface.PresentMode   presentMode = GpuSurface.PresentMode.getSupportedVsyncMode(this.windowSurface.supportedPresentModes(), this.options.enableVsync().get());
                GpuSurface.Configuration config      = new GpuSurface.Configuration(width[0], height[0], presentMode);

                try {
                    this.windowSurface.configure(config);
                    this.surfaceIsInvalid = false;
                } catch (SurfaceException exception) {
                    LOGGER.warn("Couldn't configure surface to {}: {}", config, exception);
                    this.surfaceIsInvalid = true;
                }
            }

            this.windowSurfaceNeedsReconfiguring = false;
        }

        if (this.surfaceIsInvalid || this.window.isMinimized())
            return;
        *///?}

        long acquireStart = System.nanoTime();

        try {
            this.windowSurface.acquireNextTexture();
        } catch (SurfaceException ex) {
            //? if >= 26.3 {
            LOGGER.warn("Couldn't acquire next surface texture with config {}", this.windowSurface.currentConfiguration(), ex);
            //?} else {
            /*LOGGER.warn("Couldn't acquire next surface texture with config {}: {}", this.windowSurface.currentConfiguration(), ex);
            *///?}
            this.surfaceIsInvalid                = true;
            this.windowSurfaceNeedsReconfiguring = true;
        }

        FramePath.recordAcquire(System.nanoTime() - acquireStart);
    }
}
//?}
