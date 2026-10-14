package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = RenderSystem.class, priority = 1500)
public class MixinVulkanSodiumGuard {
}
