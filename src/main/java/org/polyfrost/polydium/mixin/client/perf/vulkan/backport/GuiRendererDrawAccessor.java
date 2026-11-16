package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import net.minecraft.client.gui.render.TextureSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.render.GuiRenderer$Draw")
public interface GuiRendererDrawAccessor {
    @Accessor("textureSetup")
    TextureSetup polydium$textureSetup();
}
