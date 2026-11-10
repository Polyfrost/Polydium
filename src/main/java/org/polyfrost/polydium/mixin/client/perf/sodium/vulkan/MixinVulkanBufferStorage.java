package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.gl.functions.BufferStorageFunctions;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = BufferStorageFunctions.class, remap = false)
public class MixinVulkanBufferStorage {

    @WrapMethod(method = "pickBest")
    private static BufferStorageFunctions polydium$noGlCaps(RenderDevice device, Operation<BufferStorageFunctions> original) {
        return VulkanActive.active ? BufferStorageFunctions.NONE : original.call(device);
    }
}
