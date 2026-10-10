package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanDynamicStateReset {
    @Inject(method = "allocateAndBeginTransientCommandBuffer", at = @At("HEAD"))
    private void polydium$forgetDynamicState(CallbackInfoReturnable<VkCommandBuffer> cir) {
        VulkanDynamicState.reset();
    }
}
//?}
