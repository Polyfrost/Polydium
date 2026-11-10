package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.systems.GpuBackend;
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.VulkanAvailability;
import org.polyfrost.polydium.vulkan.EssentialVulkan;
import org.polyfrost.polydium.vulkan.VulkanBackend;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinVulkanBackendList {

    @ModifyVariable(method = "<init>", at = @At(value = "STORE", ordinal = 0))
    private GpuBackend[] polydium$tryVulkanFirst(GpuBackend[] backends) {
        if (VulkanAvailability.probe() != null)
            return backends;

        EssentialVulkan.registerMixins();
        GpuBackend[] withVulkan = new GpuBackend[backends.length + 1];
        withVulkan[0] = new VulkanBackend();
        System.arraycopy(backends, 0, withVulkan, 1, backends.length);
        return withVulkan;
    }

    @Inject(method = "resizeGui", at = @At("HEAD"))
    private void polydium$invalidateSurface(CallbackInfo ci) {
        if (VulkanDevice.active != null) {
            VulkanDevice.active.markSurfaceResized();
        }
    }
}
