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
import org.polyfrost.polydium.client.perf.FramePath;
import org.polyfrost.polydium.client.perf.PresentThread;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

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

    @ModifyExpressionValue(method = "renderFrame", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;windowSurfaceNeedsReconfiguring:Z", opcode = Opcodes.GETFIELD))
    private boolean polydium$deferAcquire(boolean needsReconfiguring) {
        if (!this.polydium$presentThread) {
            this.polydium$presentThread          = true;
            this.windowSurfaceNeedsReconfiguring = true;
            needsReconfiguring                   = true;
            FramePath.reset();
        }

        boolean worker = FramePath.beginFrame(PresentThread.canDeferAcquire(this.windowSurface));

        this.polydium$acquireDeferred = !needsReconfiguring && worker;

        return needsReconfiguring;
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface;isSuboptimal()Z"))
    private boolean polydium$skipDeferredSuboptimal(GpuSurface surface, Operation<Boolean> original) {
        if (this.polydium$acquireDeferred)
            return false;

        return original.call(surface);
    }

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
        return !PresentThread.takeHandedOff() && original.call(surface);
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
