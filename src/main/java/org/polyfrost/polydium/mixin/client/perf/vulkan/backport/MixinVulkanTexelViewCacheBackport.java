package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.client.perf.TexelViewCache;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanGpuBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanGpuBuffer.Direct.class)
public abstract class MixinVulkanTexelViewCacheBackport implements TexelViewCache {
    @Shadow
    @Final
    protected VulkanDevice device;

    @Unique
    private TexelViewCache.Views polydium$texelViews;

    @Override
    public TexelViewCache.Views polydium$texelViews() {
        if (this.polydium$texelViews == null) {
            this.polydium$texelViews = new TexelViewCache.Views();
        }
        return this.polydium$texelViews;
    }

    @Inject(method = "destroy()V", at = @At("HEAD"))
    private void polydium$destroyTexelViews(CallbackInfo ci) {
        if (this.polydium$texelViews != null) {
            this.polydium$texelViews.destroy(this.device.vkDevice());
            this.polydium$texelViews = null;
        }
    }
}
