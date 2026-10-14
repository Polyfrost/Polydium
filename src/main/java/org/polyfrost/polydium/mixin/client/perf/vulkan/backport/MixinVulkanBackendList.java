package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.systems.GpuBackend;
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.vulkan.VulkanBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public class MixinVulkanBackendList {

    @ModifyVariable(method = "<init>", at = @At(value = "STORE", ordinal = 0))
    private GpuBackend[] polydium$tryVulkanFirst(GpuBackend[] backends) {
        if (VulkanBackend.checkBackendAvailable() != null)
            return backends;

        org.polyfrost.polydium.vulkan.EssentialVulkan.registerMixins();
        GpuBackend[] withVulkan = new GpuBackend[backends.length + 1];
        withVulkan[0] = new VulkanBackend();
        System.arraycopy(backends, 0, withVulkan, 1, backends.length);
        return withVulkan;
    }
}
