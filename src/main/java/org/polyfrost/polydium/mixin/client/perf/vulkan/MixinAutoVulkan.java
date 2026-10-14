package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.device.GpuDevice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.PreferredGraphicsApi;
import net.minecraft.client.main.GameConfig;
import org.polyfrost.polydium.early.EarlyVulkan;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Minecraft.class)
public abstract class MixinAutoVulkan {

    @Shadow
    @Final
    public Options options;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderer"))
    private GpuDevice polydium$undoFailedVulkan(GpuDevice device, @Local(argsOnly = true) GameConfig config) {
        if (config.game.forcedGraphicsApi == null && this.options.preferredGraphicsBackend().get() == PreferredGraphicsApi.VULKAN
                && !"Vulkan".equalsIgnoreCase(device.getDeviceInfo().backendName()) && EarlyVulkan.switched()) {
            this.options.preferredGraphicsBackend().set(PreferredGraphicsApi.DEFAULT);
            this.options.save();
        }
        return device;
    }
}
//?}
//? if >= 1.21.11 && < 26.2 {
/*import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.early.EarlyVulkan;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinAutoVulkan {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polydium$undoFailedVulkan(CallbackInfo ci) {
        if (EarlyVulkan.vulkanAtStartup() && !VulkanActive.active && EarlyVulkan.switched())
            EarlyVulkan.setGraphicsApi("default");
    }
}
*///?}
