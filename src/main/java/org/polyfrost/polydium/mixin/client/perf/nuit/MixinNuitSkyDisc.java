package org.polyfrost.polydium.mixin.client.perf.nuit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.injection.At;
//?}

@Pseudo
@Mixin(targets = "me.flashyreese.mods.nuit.SkyboxManager$1", remap = false)
public class MixinNuitSkyDisc {
    //? if >= 26.3 {
    @WrapOperation(
            method = "*",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getCompiledPipeline(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline;"),
            require = 0
    )
    private static CompiledRenderPipeline polydium$farNuitDiscPipeline(RenderPipeline pipeline, Operation<CompiledRenderPipeline> original) {
        return original.call(SkyFarPlane.isDrawing() ? SkyFarPlane.far(pipeline) : pipeline);
    }
    //?}
}
