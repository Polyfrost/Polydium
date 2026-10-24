package org.polyfrost.polydium.mixin.client.perf.overflow;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if >= 26.2 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
//?}

@Pseudo
@Mixin(targets = "org.polyfrost.overflowanimations.renderer.impl.AbstractRenderer", remap = false)
public class MixinAbstractRenderer {
    //? if >= 26.2 {
    @ModifyVariable(
            method = "setPipeline(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lorg/polyfrost/overflowanimations/handler/compatibility/IrisPipeline;)Lorg/polyfrost/overflowanimations/renderer/impl/AbstractRenderer;",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private RenderPipeline polydium$farPipeline(RenderPipeline pipeline) {
        return SkyFarPlane.isDrawing() ? SkyFarPlane.far(pipeline) : pipeline;
    }
    //?}
}
