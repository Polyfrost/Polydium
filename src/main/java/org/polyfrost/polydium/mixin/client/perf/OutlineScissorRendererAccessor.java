package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RenderTypeFeatureRenderer.class)
public interface OutlineScissorRendererAccessor {
    @Accessor("groups")
    List<?> polydium$groups();
}
//?}
