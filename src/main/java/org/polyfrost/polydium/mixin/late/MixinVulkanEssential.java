package org.polyfrost.polydium.mixin.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
    "gg.essential.util.GpuTextureImpl",
    "gg.essential.util.GuiRenderSetupKt",
    "gg.essential.gui.common.UI3DPlayer",
    "gg.essential.gui.screenshot.image.PixelBufferTexture",
    "gg.essential.gui.screenshot.providers.MinecraftWindowedTextureProviderKt"
}, remap = false)
public class MixinVulkanEssential {
}
