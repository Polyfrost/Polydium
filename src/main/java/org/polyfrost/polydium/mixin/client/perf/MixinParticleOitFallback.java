package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.QuadParticleFeatureRenderer;
import net.minecraft.client.renderer.oit.OitStage;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(QuadParticleFeatureRenderer.class)
public class MixinParticleOitFallback {
    @WrapMethod(method = "getOitPipeline")
    private static RenderPipeline polydium$fallbackOitPipeline(OitStage stage, SingleQuadParticle.Layer layer, Operation<RenderPipeline> original) {
        if (layer.oitPipelineSet() == null) {
            return RenderPipelines.OIT_PARTICLE.getPipeline(stage);
        }
        return original.call(stage, layer);
    }
}
//?}
