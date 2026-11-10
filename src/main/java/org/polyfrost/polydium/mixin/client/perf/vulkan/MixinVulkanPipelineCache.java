package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import org.lwjgl.vulkan.VkDevice;
import org.polyfrost.polydium.client.perf.VulkanPipelineCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanDevice.class)
public abstract class MixinVulkanPipelineCache implements VulkanPipelineCache.Holder {
    @Shadow
    @Final
    private VkDevice vkDevice;

    @Unique
    private final VulkanPipelineCache polydium$pipelineCache = new VulkanPipelineCache();

    @Override
    public VulkanPipelineCache polydium$pipelineCache() {
        return this.polydium$pipelineCache;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polydium$createPipelineCache(CallbackInfo ci) {
        this.polydium$pipelineCache.create(this.vkDevice);
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkDestroyDevice(Lorg/lwjgl/vulkan/VkDevice;Lorg/lwjgl/vulkan/VkAllocationCallbacks;)V"))
    private void polydium$destroyPipelineCache(CallbackInfo ci) {
        this.polydium$pipelineCache.destroy(this.vkDevice);
    }
}
//?}
