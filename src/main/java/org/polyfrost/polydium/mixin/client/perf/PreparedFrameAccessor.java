package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(targets = "net.minecraft.client.renderer.feature.FeatureRenderDispatcher$PreparedFrame")
public interface PreparedFrameAccessor {
    @Accessor("context")
    FeatureFrameContext polydium$context();

    @Accessor("allSubmits")
    List<?> polydium$allSubmits();

    @Accessor("groupsByPhase")
    Map<?, ?> polydium$groupsByPhase();

    @Accessor("groupsByFeature")
    Map<?, ?> polydium$groupsByFeature();
}
//?}
