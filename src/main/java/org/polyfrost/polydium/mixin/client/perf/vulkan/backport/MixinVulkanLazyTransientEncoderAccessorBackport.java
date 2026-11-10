package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VulkanCommandEncoder.class)
public interface MixinVulkanLazyTransientEncoderAccessorBackport {
    @Accessor("submissionBuilder")
    VulkanQueue.Submission polydium$submissionBuilder();
}
