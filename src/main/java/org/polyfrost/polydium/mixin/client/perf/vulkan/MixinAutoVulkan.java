package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.device.GpuDevice;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.PreferredGraphicsApi;
import net.minecraft.client.main.GameConfig;
import org.polyfrost.polydium.early.EarlyVulkan;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
        if (config.game.forcedGraphicsApi == null && this.options.preferredGraphicsBackend().get() == PreferredGraphicsApi.VULKAN
                && !"Vulkan".equalsIgnoreCase(device.getDeviceInfo().backendName()) && EarlyVulkan.switched()) {
            this.options.preferredGraphicsBackend().set(PreferredGraphicsApi.DEFAULT);
            this.options.save();
        }
        return device;
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
        if (EarlyVulkan.vulkanAtStartup() && !VulkanActive.active && EarlyVulkan.switched())
            EarlyVulkan.setGraphicsApi("default");
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

    @Inject(method = "stop", at = @At("HEAD"))
    private void polydium$keepVulkanOnLoadingClose(CallbackInfo ci) {
        if (!this.gameLoadFinished && this.options != null) {
            this.options.startedCleanly = true;
            this.options.save();
        }
    }
}
*///?}
