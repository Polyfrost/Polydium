package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.polyfrost.polydium.client.perf.VulkanDeferral;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanSampledAttachmentBackport {
    @Shadow
    @Final
    private VulkanCommandEncoder encoder;

    @Inject(method = "bindTexture", at = @At("HEAD"))
    private void polydium$checkSampledAttachment(String name, GpuTextureView view, GpuSampler sampler, CallbackInfo ci) {
        if (view != null && (Object) this.encoder instanceof VulkanDeferral.Host host) {
            host.polydium$sampled(view);
        }
    }
}
