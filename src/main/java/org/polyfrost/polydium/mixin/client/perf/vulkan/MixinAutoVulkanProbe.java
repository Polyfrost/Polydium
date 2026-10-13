package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.api.device.DeviceType;
import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanPhysicalDevice;
import org.polyfrost.polydium.client.perf.AutoVulkan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VulkanBackend.class)
public abstract class MixinAutoVulkanProbe {

    //? if >= 26.3 {
    @ModifyExpressionValue(method = "checkBackendAvailableWithLoadedLibrary", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanBackend;findPhysicalDevice"))
    //?} else {
    /*@ModifyExpressionValue(method = "checkBackendAvailable", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanBackend;findPhysicalDevice"))
    *///?}
    private static VulkanPhysicalDevice polydium$recordDeviceType(VulkanPhysicalDevice device) {
        AutoVulkan.discrete = device.deviceType() == DeviceType.DISCRETE;
        return device;
    }

    @Inject(method = "checkBackendAvailable", at = @At("RETURN"))
    private static void polydium$recordProbeResult(CallbackInfoReturnable<?> cir) {
        AutoVulkan.available = cir.getReturnValue() == null;
    }
}
//?}
