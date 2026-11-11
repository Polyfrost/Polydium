package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.textures.GpuTexture;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.polyfrost.polydium.vulkan.XaeroVulkan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
    "xaero.common.graphics.OpenGlHelper",
    "xaero.map.graphics.OpenGlHelper",
    "xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider",
    "xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider"
}, remap = false)
public class MixinVulkanXaero {

    @Inject(method = "generateMipmaps", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$generateMipmaps(GpuTexture texture, CallbackInfo ci) {
        if (!VulkanActive.active) return;
        XaeroVulkan.generateMipmaps(texture);
        ci.cancel();
    }

    @Inject(method = "defaultTextureBind", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$bindOnPass(GpuTexture texture, CallbackInfo ci) {
        if (!VulkanActive.active) return;
        VulkanRenderPass pass = VulkanRenderPass.multiDrawing;
        if (pass != null && texture != null) pass.rebindSampler0(texture);
        ci.cancel();
    }
}
