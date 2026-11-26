package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface;
import org.polyfrost.polydium.client.perf.VulkanDeferral;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanGpuSurface.class)
public abstract class MixinVulkanBlitLayout {
    @Inject(method = "blitFromTexture", at = @At("HEAD"))
    private void polydium$generalForBlit(CommandEncoderBackend commandEncoder, GpuTextureView textureView, CallbackInfo ci) {
        if (commandEncoder instanceof VulkanDeferral.Host host) {
            host.polydium$general(textureView.texture());
        }
    }
}
//?}
