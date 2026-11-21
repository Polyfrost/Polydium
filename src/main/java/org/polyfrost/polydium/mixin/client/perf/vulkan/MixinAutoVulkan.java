package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.device.GpuBackend;
import com.mojang.renderpearl.api.device.GpuDevice;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.PreferredGraphicsApi;
import net.minecraft.client.main.GameConfig;
import org.polyfrost.polydium.early.EarlyVulkan;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MixinAutoVulkan {

    @Shadow
    @Final
    public Options options;

    @Shadow
    private boolean gameLoadFinished;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderer"))
    private GpuDevice polydium$undoFailedVulkan(GpuDevice device, @Local(argsOnly = true) GameConfig config) {
        if (config.game.forcedGraphicsApi != null || !EarlyVulkan.switchPending())
            return device;
        if (this.options.preferredGraphicsBackend().get() == PreferredGraphicsApi.VULKAN
                && !"Vulkan".equalsIgnoreCase(device.getDeviceInfo().backendName())) {
            EarlyVulkan.markFailed("automatic Vulkan switch failed, started on " + device.getDeviceInfo().backendName());
            this.options.preferredGraphicsBackend().set(PreferredGraphicsApi.DEFAULT);
            this.options.save();
            return device;
        }
        if (this.options.preferredGraphicsBackend().get() == PreferredGraphicsApi.VULKAN)
            EarlyVulkan.setGraphicsApi("vulkan");
        EarlyVulkan.finishSwitch();
        return device;
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;set(Ljava/lang/Object;)V", ordinal = 0), slice = @Slice(
        from = @At(value = "CONSTANT", args = "stringValue=Detected unexpected shutdown during last game startup: resetting preferred graphics API to Default")))
    private void polydium$markVanillaReset(OptionInstance<Object> option, Object value, Operation<Void> original) {
        if (!EarlyVulkan.vulkanStartUnfinished()) {
            LoggerFactory.getLogger("polydium/vulkan").info("Keeping Vulkan: the unfinished startup was not a Vulkan start");
            return;
        }
        EarlyVulkan.markFailed("last game startup did not finish");
        original.call(option, value);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/PreferredGraphicsApi;getBackendsToTry()[Lcom/mojang/renderpearl/api/device/GpuBackend;"))
    private GpuBackend[] polydium$markVulkanStart(PreferredGraphicsApi api, Operation<GpuBackend[]> original) {
        EarlyVulkan.markVulkanStart(api == PreferredGraphicsApi.VULKAN);
        return original.call(api);
    }

    @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
    private void polydium$finishVulkanStart(CallbackInfo ci) {
        EarlyVulkan.markVulkanStart(false);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;set(Ljava/lang/Object;)V", ordinal = 0), slice = @Slice(
        from = @At(value = "CONSTANT", args = "stringValue=Detected unexpected shutdown during last game startup: forcing preferred graphics API to OpenGL")))
    private void polydium$markVanillaOpenGl(CallbackInfo ci) {
        EarlyVulkan.vanillaOpenGl = true;
    }

    @Inject(method = "saveReport(Ljava/io/File;Lnet/minecraft/CrashReport;I)I", at = @At("HEAD"))
    private static void polydium$keepVulkanOnJavaCrash(CallbackInfoReturnable<Integer> cir, @Local(argsOnly = true) CrashReport crash) {
        if (EarlyVulkan.renderCrash(crash.getException()))
            return;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.options != null)
            mc.options.startedCleanly = true;
        EarlyVulkan.markStartClean();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void polydium$keepVulkanOnLoadingClose(CallbackInfo ci) {
        if (!this.gameLoadFinished && this.options != null) {
            this.options.startedCleanly = true;
            this.options.save();
            EarlyVulkan.markVulkanStart(false);
        }
    }
}
//?}
//? if >= 1.21.11 && < 26.2 {
/*import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.polyfrost.polydium.early.EarlyVulkan;
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MixinAutoVulkan {

    @Shadow
    @Final
    public Options options;

    @Shadow
    private boolean gameLoadFinished;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polydium$undoFailedVulkan(CallbackInfo ci) {
        if (PolydiumMixinPlugin.SODIUM_BLOCKS_VULKAN) {
            EarlyVulkan.retrySodiumBlockedFailure();
            return;
        }
        if (!EarlyVulkan.switchPending())
            return;
        if (EarlyVulkan.vulkanAtStartup() && !VulkanActive.active) {
            EarlyVulkan.markFailed("automatic Vulkan switch failed");
            EarlyVulkan.setGraphicsApi("default");
            return;
        }
        EarlyVulkan.finishSwitch();
    }

    @Inject(method = "saveReport(Ljava/io/File;Lnet/minecraft/CrashReport;)I", at = @At("HEAD"))
    private static void polydium$keepVulkanOnJavaCrash(CallbackInfoReturnable<Integer> cir, @Local(argsOnly = true) CrashReport crash) {
        if (EarlyVulkan.renderCrash(crash.getException()))
            return;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.options != null)
            mc.options.startedCleanly = true;
        EarlyVulkan.markStartClean();
    }

    @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
    private void polydium$finishVulkanStart(CallbackInfo ci) {
        EarlyVulkan.markVulkanStart(false);
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void polydium$keepVulkanOnLoadingClose(CallbackInfo ci) {
        if (!this.gameLoadFinished && this.options != null) {
            this.options.startedCleanly = true;
            this.options.save();
            EarlyVulkan.markVulkanStart(false);
        }
    }
}
*///?}
