package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
//? if >= 26.3
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import org.polyfrost.polydium.client.perf.VulkanLegacyRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(VulkanDevice.class)
public abstract class MixinVulkanLegacyDevice implements VulkanLegacyRendering.Device {
    @Unique
    private boolean polydium$dynamicRendering;

    @Unique
    private boolean polydium$pushDescriptors;

    @Unique
    private final VulkanLegacyRendering.RenderPassCache polydium$renderPassCache = new VulkanLegacyRendering.RenderPassCache((VulkanDevice) (Object) this);

    @Override
    public boolean polydium$dynamicRendering() {
        return this.polydium$dynamicRendering;
    }

    @Override
    public boolean polydium$pushDescriptors() {
        return this.polydium$pushDescriptors;
    }

    @Override
    public VulkanLegacyRendering.RenderPassCache polydium$renderPassCache() {
        return this.polydium$renderPassCache;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    //? if >= 26.3 {
    private void polydium$detectExtensions(CallbackInfo ci, @Local(argsOnly = true) FeatureSet enabledFeatures) {
        Set<String> extensions = enabledFeatures.extensions();
    //?} else {
    /*private void polydium$detectExtensions(CallbackInfo ci, @Local(argsOnly = true) Set<String> extensions) {
    *///?}
        this.polydium$dynamicRendering = extensions.contains("VK_KHR_dynamic_rendering");
        this.polydium$pushDescriptors = extensions.contains("VK_KHR_push_descriptor");
        LogUtils.getLogger().info(
            "Vulkan using {} and {}",
            this.polydium$dynamicRendering ? "dynamic rendering" : "render passes",
            this.polydium$pushDescriptors ? "push descriptors" : "descriptor sets"
        );
    }

    // After the encoder has waited for the GPU, unlike 26.4-snapshot-1 which destroys them first
    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/Vma;vmaDestroyAllocator(J)V"))
    private void polydium$destroyRenderPasses(CallbackInfo ci) {
        this.polydium$renderPassCache.destroy();
    }
}
//?}
