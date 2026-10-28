package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import net.minecraft.client.renderer.feature.FeatureRendererType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.feature.FeatureRenderDispatcher$PreparedGroup")
public interface OutlineScissorGroupAccessor {
    @Accessor("featureType")
    FeatureRendererType<?> polydium$featureType();

    @Accessor("featureGroupIndex")
    int polydium$featureGroupIndex();
}
//?}
