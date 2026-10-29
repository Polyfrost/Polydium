package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.renderer.entity.DisplayRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = DisplayRenderer.TextDisplayRenderer.class, priority = 999)
public abstract class MixinTextDisplaySplitter {
}
