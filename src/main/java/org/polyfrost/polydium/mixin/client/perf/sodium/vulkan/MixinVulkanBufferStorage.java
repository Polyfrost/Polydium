package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.gl.functions.BufferStorageFunctions;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BufferStorageFunctions.class, remap = false)
public class MixinVulkanBufferStorage {

    @Inject(method = "pickBest", at = @At("HEAD"), cancellable = true)
    private static void polydium$noGlCaps(RenderDevice device, CallbackInfoReturnable<BufferStorageFunctions> cir) {
        if (VulkanActive.active)
            cir.setReturnValue(BufferStorageFunctions.NONE);
    }
}
