package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.GLRenderDevice;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.sodium.VkCommandList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GLRenderDevice.class, remap = false)
public class MixinVulkanRenderDevice {

    @Inject(method = "createCommandList", at = @At("HEAD"), cancellable = true)
    private void polydium$vulkanCommandList(CallbackInfoReturnable<CommandList> cir) {
        if (VulkanActive.active)
            cir.setReturnValue(VkCommandList.INSTANCE);
    }
}
