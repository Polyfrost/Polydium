package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import net.minecraft.client.Minecraft;
import org.polyfrost.oneconfig.internal.ui.compose.SkiaCtx;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinVulkanBackendListOneConfig {
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen()V"))
    private void polydium$drawSkiaBeforePresent(boolean tick, CallbackInfo ci) {
        if (VulkanActive.active) SkiaCtx.INSTANCE.draw();
    }
}
