package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRendererMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FeatureRenderDispatcher.class)
public interface OutlineScissorDispatcherAccessor {
    @Accessor("featureRenderers")
    FeatureRendererMap polydium$featureRenderers();
}
//?}
