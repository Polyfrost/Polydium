package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "io.wispforest.owo.braid.core.BraidWindow", remap = false)
public class MixinVulkanOwoBraid {
}
