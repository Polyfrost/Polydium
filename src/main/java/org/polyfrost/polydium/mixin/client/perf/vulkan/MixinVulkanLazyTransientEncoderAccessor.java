package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VulkanCommandEncoder.class)
public interface MixinVulkanLazyTransientEncoderAccessor {
    @Accessor("submissionBuilder")
    VulkanQueue.Submission polydium$submissionBuilder();
}
//?}
