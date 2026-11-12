package org.polyfrost.polydium.mixin.client;

//? if >= 1.21.11 {
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.VulkanPrompt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.api.device.GpuBackend;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
//? if < 26.3 {
/*import com.mojang.blaze3d.shaders.GpuDebugOptions;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
*///?}
import net.minecraft.client.Options;
import net.minecraft.client.PreferredGraphicsApi;
import net.minecraft.client.main.GameConfig;
import org.jspecify.annotations.Nullable;
import org.polyfrost.polydium.client.VulkanAvailability;
import org.polyfrost.polydium.early.EarlyVulkan;
import org.polyfrost.polydium.early.VulkanProbe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
//?}

@Mixin(Minecraft.class)
public abstract class MixinVulkanStartupProbe {
    //? if >= 26.2 {
    @Shadow
    @Final
    public Options options;
    //?}

    // Right before the window is created, after vanilla 26.2+ has run its own probe for the Default graphics API
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createTitle()Ljava/lang/String;"))
    private void polydium$probeVulkan(CallbackInfo ci) {
        VulkanPrompt.probeAtStartup();
    }

    //? if >= 26.2 {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanBackend;checkBackendAvailable()Lcom/mojang/renderpearl/api/device/BackendCreationException;"))
    private @Nullable BackendCreationException polydium$recordVanillaProbe(Operation<BackendCreationException> original) {
        String failure = VulkanProbe.failure();
        BackendCreationException unavailable = failure == null ? original.call()
                : new BackendCreationException(failure, BackendCreationException.Reason.OTHER);
        VulkanAvailability.record(unavailable == null ? null : unavailable.getMessage());
        return unavailable;
    }

    //? if >= 26.3 {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuBackend;loadLibrary()V"))
    private void polydium$skipCrashingVulkan(GpuBackend backend, Operation<Void> original, @Local(argsOnly = true) GameConfig config) throws BackendCreationException {
        polydium$checkVulkanProbe(backend, config);
        original.call(backend);
    }
    //?} else {
    /*@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuBackend;createDevice(JLcom/mojang/blaze3d/shaders/ShaderSource;Lcom/mojang/blaze3d/shaders/GpuDebugOptions;Ljava/lang/Runnable;)Lcom/mojang/blaze3d/systems/GpuDevice;"))
    private GpuDevice polydium$skipCrashingVulkan(GpuBackend backend, long window, ShaderSource shaders, GpuDebugOptions debug, Runnable callback, Operation<GpuDevice> original, @Local(argsOnly = true) GameConfig config) throws BackendCreationException {
        polydium$checkVulkanProbe(backend, config);
        return original.call(backend, window, shaders, debug, callback);
    }
    *///?}

    @Unique
    private void polydium$checkVulkanProbe(GpuBackend backend, GameConfig config) throws BackendCreationException {
        String failure = backend instanceof VulkanBackend ? VulkanProbe.failure() : null;
        if (failure == null)
            return;
        VulkanAvailability.record(failure);
        if (failure.equals(VulkanProbe.CRASHED) && config.game.forcedGraphicsApi == null
                && this.options.preferredGraphicsBackend().get() == PreferredGraphicsApi.VULKAN) {
            EarlyVulkan.markFailed(failure);
            this.options.preferredGraphicsBackend().set(PreferredGraphicsApi.DEFAULT);
            this.options.save();
        }
        throw new BackendCreationException(failure, BackendCreationException.Reason.OTHER);
    }
    //?}
}
//?}
