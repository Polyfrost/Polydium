package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(targets = "org.polyfrost.polydium.vulkan.VulkanQueue$Submission$SubmitStage")
public interface MixinVulkanLazyTransientStageAccessorBackport {
    @Accessor("waits")
    List<?> polydium$waits();

    @Accessor("commandBuffers")
    List<VkCommandBuffer> polydium$commandBuffers();
}
