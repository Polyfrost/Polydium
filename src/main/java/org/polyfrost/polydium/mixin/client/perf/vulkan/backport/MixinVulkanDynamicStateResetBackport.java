package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanDynamicStateResetBackport {
    @Inject(method = "allocateAndBeginTransientCommandBuffer", at = @At("HEAD"))
    private void polydium$forgetDynamicState(CallbackInfoReturnable<VkCommandBuffer> cir) {
        VulkanDynamicState.reset();
    }
}
