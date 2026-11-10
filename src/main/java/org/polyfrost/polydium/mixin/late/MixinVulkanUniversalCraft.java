package org.polyfrost.polydium.mixin.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
    "gg.essential.universal.render.UGpuDeviceImpl",
    "gg.essential.universal.render.UGpuDeviceImpl$info$1",
    "gg.essential.universal.render.UGpuDeviceImpl$info$1$limits$1",
    "gg.essential.universal.render.UGpuFenceImplKt"
}, remap = false)
public class MixinVulkanUniversalCraft {
}
