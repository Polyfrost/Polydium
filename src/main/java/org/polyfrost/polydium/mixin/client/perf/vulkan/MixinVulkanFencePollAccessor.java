package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = {"com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder$1", "com.mojang.renderpearl.backend.opengl.GlFence"})
public interface MixinVulkanFencePollAccessor {
    @Accessor("submitIndex")
    long polydium$submitIndex();
}
//?}
