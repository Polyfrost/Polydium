package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import org.lwjgl.vulkan.VK10;
import org.polyfrost.polydium.client.perf.TexelViewCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanGpuBuffer.Direct.class)
public abstract class MixinVulkanTexelViewCache implements TexelViewCache {
    @Unique
    private static final int POLYDIUM$MAX_VIEWS = 8;

    @Shadow
    @Final
    protected VulkanDevice device;

    @Unique
    private long[] polydium$texelViews;

    @Unique
    private int polydium$texelViewCount;

    @Override
    public long polydium$findTexelView(long offset, long range, int format) {
        long[] views = this.polydium$texelViews;
        for (int i = 0; i < this.polydium$texelViewCount; i++) {
            int base = i * 4;
            if (views[base] == offset && views[base + 1] == range && views[base + 2] == format) {
                return views[base + 3];
            }
        }
        return 0L;
    }

    @Override
    public boolean polydium$storeTexelView(long offset, long range, int format, long view) {
        if (this.polydium$texelViewCount == POLYDIUM$MAX_VIEWS) {
            return false;
        }
        if (this.polydium$texelViews == null) {
            this.polydium$texelViews = new long[POLYDIUM$MAX_VIEWS * 4];
        }
        int base = this.polydium$texelViewCount++ * 4;
        this.polydium$texelViews[base] = offset;
        this.polydium$texelViews[base + 1] = range;
        this.polydium$texelViews[base + 2] = format;
        this.polydium$texelViews[base + 3] = view;
        return true;
    }

    @Inject(method = "destroy()V", at = @At("HEAD"))
    private void polydium$destroyTexelViews(CallbackInfo ci) {
        for (int i = 0; i < this.polydium$texelViewCount; i++) {
            VK10.vkDestroyBufferView(this.device.vkDevice(), this.polydium$texelViews[i * 4 + 3], null);
        }
        this.polydium$texelViewCount = 0;
        this.polydium$texelViews = null;
    }
}
//?}
