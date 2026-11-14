package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinVulkanBackendListLeakedPass {
    @Inject(method = "runTick", at = @At("HEAD"))
    private void polydium$closeLeakedPassAtFrameStart(CallbackInfo ci) {
        if (VulkanDevice.active != null)
            VulkanDevice.active.createCommandEncoder().closeLeakedRenderPass();
    }

    //? if >= 26.1 {
    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen()V"))
    //?} else {
    /*@Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen()V"))
    *///?}
    private void polydium$closeLeakedPassBeforePresent(CallbackInfo ci) {
        if (VulkanDevice.active != null)
            VulkanDevice.active.createCommandEncoder().closeLeakedRenderPass();
    }
}
