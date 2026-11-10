package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
import org.polyfrost.polydium.client.perf.OutlineScissor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(FeatureRenderDispatcher.PreparedFrame.class)
public abstract class MixinOutlineScissorFrame {
    @Shadow
    private SubmitNodeStorage submitNodeStorage;

    @Shadow
    @Final
    private Map<FeatureRenderPhase<?>, List<?>> groupsByPhase;

    @Shadow
    @Final
    FeatureRenderDispatcher this$0;

    @Inject(method = "executeOutline", at = @At("HEAD"))
    private void polydium$measureOutline(CallbackInfo ci) {
        OutlineScissor.measure(this.submitNodeStorage, this.groupsByPhase, ((OutlineScissorDispatcherAccessor) this.this$0).polydium$featureRenderers());
    }
}
//?}
