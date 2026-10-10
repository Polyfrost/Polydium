package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import org.lwjgl.vulkan.VkCommandBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(targets = "com.mojang.renderpearl.backend.vulkan.VulkanQueue$Submission$SubmitStage")
public interface MixinVulkanLazyTransientStageAccessor {
    @Accessor("waits")
    List<?> polydium$waits();

    @Accessor("commandBuffers")
    List<VkCommandBuffer> polydium$commandBuffers();
}
//?}
