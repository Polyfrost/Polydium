package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
    "dev.dediamondpro.resourcify.libs.universal.render.UGpuDeviceImpl",
    "dev.dediamondpro.resourcify.libs.universal.render.UGpuDeviceImpl$info$1",
    "dev.dediamondpro.resourcify.libs.universal.render.UGpuDeviceImpl$info$1$limits$1",
    "dev.dediamondpro.resourcify.libs.universal.render.UGpuFenceImplKt"
}, remap = false)
public class MixinVulkanUniversalCraft {
}
