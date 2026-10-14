package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.opengl.GlStateManager;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinVulkanBackendListGlState {

    @Inject(method = {
        "_enableBlend", "_disableBlend", "_blendFuncSeparate",
        "_enableDepthTest", "_disableDepthTest", "_depthMask",
        "_enableCull", "_disableCull", "_colorMask",
        "_enableScissorTest", "_disableScissorTest", "_scissorBox"
    }, at = @At("HEAD"), cancellable = true)
    private static void polydium$skipOnVulkan(CallbackInfo ci) {
        if (VulkanActive.active) ci.cancel();
    }
}
