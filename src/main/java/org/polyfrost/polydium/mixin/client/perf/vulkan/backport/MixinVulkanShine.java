package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
    "com.bloom.client.render.BloomSourceRenderer",
    "com.bloom.client.rimlight.render.RimLightMaskRenderer",
    "com.bloom.client.coloredlight.ColoredLightRenderer",
    "com.bloom.client.coloredlight.ColoredLightRenderer$UniformLocations"
}, remap = false)
public class MixinVulkanShine {
}
