package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

//? if >= 26.1 {
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
    "com.odtheking.odin.utils.ui.rendering.NVGRenderer",
    "com.odtheking.odin.utils.ui.rendering.NVGPIPRenderer$Companion"
}, remap = false)
public class MixinVulkanOdin {
}
//?}
