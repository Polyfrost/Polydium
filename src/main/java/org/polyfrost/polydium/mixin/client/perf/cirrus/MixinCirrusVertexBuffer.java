package org.polyfrost.polydium.mixin.client.perf.cirrus;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 26.3 {
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
//?} else
/*import com.mojang.renderpearl.api.commands.RenderPass;*/

@Pseudo
@Mixin(targets = "com.jvn.cirrus.client.render.CirrusVertexBuffer", remap = false)
public class MixinCirrusVertexBuffer {
    //? if >= 26.3 {
    @WrapOperation(
            method = "*",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getCompiledPipeline(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline;"),
            require = 0
    )
    private CompiledRenderPipeline polydium$farCirrusPipeline(RenderPipeline pipeline, Operation<CompiledRenderPipeline> original) {
        return original.call(SkyFarPlane.isDrawing() ? SkyFarPlane.farKeepingDepthWrite(pipeline) : pipeline);
    }
    //?} else {
    /*@WrapOperation(
            method = "*",
            at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setPipeline(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)V"),
            require = 0
    )
    private void polydium$farCirrusPipeline(RenderPass pass, RenderPipeline pipeline, Operation<Void> original) {
        original.call(pass, SkyFarPlane.isDrawing() ? SkyFarPlane.farKeepingDepthWrite(pipeline) : pipeline);
    }
    *///?}
}
//?}
