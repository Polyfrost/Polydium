package org.polyfrost.polydium.mixin.client.perf.skyboxify;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if skyboxify && >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.injection.At;
//?}
//? if skyboxify && >= 26.3 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import org.spongepowered.asm.mixin.Unique;

import java.util.IdentityHashMap;
import java.util.Map;
//?}

@Pseudo
@Mixin(targets = "btw.lowercase.skyboxify.skybox.renderer.SkyFeatureRenderer", remap = false)
public class MixinSkyFeatureRenderer {
    //? if skyboxify && >= 26.3 {
    @Unique
    private final Map<CompiledRenderPipeline, RenderPipeline> polydium$sources = new IdentityHashMap<>();

    @WrapOperation(
            method = "createSubmit",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getCompiledPipeline(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline;"),
            require = 0
    )
    private CompiledRenderPipeline polydium$rememberSource(RenderPipeline pipeline, Operation<CompiledRenderPipeline> original) {
        CompiledRenderPipeline compiled = original.call(pipeline);
        this.polydium$sources.put(compiled, pipeline);
        return compiled;
    }

    @WrapOperation(
            method = "endFrame",
            at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setPipeline(Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline;)V"),
            require = 0
    )
    private void polydium$farPipeline(RenderPass pass, CompiledRenderPipeline pipeline, Operation<Void> original) {
        RenderPipeline source = SkyFarPlane.isDrawing() ? this.polydium$sources.get(pipeline) : null;
        original.call(pass, source == null ? pipeline : RenderSystem.getCompiledPipeline(SkyFarPlane.far(source)));
    }
    //?} else if skyboxify && >= 1.21.10 {
    /*@WrapOperation(
            method = "endFrame",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V"),
            require = 0
    )
    private void polydium$farPipeline(RenderPass pass, RenderPipeline pipeline, Operation<Void> original) {
        original.call(pass, SkyFarPlane.isDrawing() ? SkyFarPlane.far(pipeline) : pipeline);
    }
    *///?}
}
