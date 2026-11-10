package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
    "journeymap.client.texture.gl.GlRegionTexture",
    "journeymap.client.texture.gl.GlMaskedTexture",
    "journeymap.client.render.RenderWrapper",
    "journeymap.client.render.JMRenderTypes",
    "journeymap.client.texture.RegionTexture",
    "journeymap.client.texture.LodTexture",
    "journeymap.client.task.main.ExpireTextureTask"
}, remap = false)
public class MixinVulkanJourneyMap {

    @Inject(method = "createTexture", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$createTexture(String label, int usage, TextureFormat format, int width, int height, int depthOrLayers, int mipLevels, CallbackInfoReturnable<GpuTexture> cir) {
        if (VulkanActive.active) cir.setReturnValue(RenderSystem.getDevice().createTexture(label, usage, format, width, height, depthOrLayers, mipLevels));
    }

    @Inject(method = {"enableBlend", "disableBlend", "blendFuncSeparate", "blendFunc", "enableDepthTest", "disableDepthTest", "texParameter",
        "activeTexture", "bindTexture", "clear", "depthFunc", "texSubImage2D", "clearDepth"}, at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$skipGl(CallbackInfo ci) {
        if (VulkanActive.active) ci.cancel();
    }

    @Inject(method = "getError", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$getError(CallbackInfoReturnable<Integer> cir) {
        if (VulkanActive.active) cir.setReturnValue(0);
    }

    @Inject(method = "deleteTexture(I)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void polydium$deleteTexture(int textureId, CallbackInfoReturnable<Boolean> cir) {
        if (VulkanActive.active) cir.setReturnValue(true);
    }
}
