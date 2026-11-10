package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "de.keksuccino.fancymenu.util.rendering.GuiBlurRenderer$PostPassScissor", remap = false)
public abstract class MixinVulkanFancyMenuScissor implements VulkanActive.Scissor {
}
