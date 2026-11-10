package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
//? if >= 26.3 {
import com.mojang.renderpearl.util.TextureViewAndSampler;
//?} else {
/*import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
*///?}
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import org.polyfrost.polydium.client.perf.VulkanDeferral;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanSampledAttachment {
    @Shadow
    @Final
    private VulkanCommandEncoder encoder;

    //? if >= 26.3 {
    @Inject(method = "setUniform(ILjava/lang/Object;)V", at = @At("HEAD"))
    private void polydium$checkSampledAttachment(int index, Object value, CallbackInfo ci) {
        if (value instanceof TextureViewAndSampler bound && (Object) this.encoder instanceof VulkanDeferral.Host host) {
            host.polydium$sampled(bound.view());
        }
    }
    //?} else {
    /*@Inject(method = "bindTexture", at = @At("HEAD"))
    private void polydium$checkSampledAttachment(String name, GpuTextureView view, GpuSampler sampler, CallbackInfo ci) {
        if (view != null && (Object) this.encoder instanceof VulkanDeferral.Host host) {
            host.polydium$sampled(view);
        }
    }
    *///?}
}
//?}
