package org.polyfrost.polydium.mixin.client.perf.nuit;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 26.3 {
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import org.joml.Vector4fc;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
//?}

@Pseudo
@Mixin(targets = "me.flashyreese.mods.nuit.render.NuitRenderBackend", remap = false)
public class MixinNuitRenderBackend {
    //? if >= 26.3 {
    @WrapOperation(
            method = "*",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getCompiledPipeline(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline;"),
            require = 0
    )
    private static CompiledRenderPipeline polydium$farNuitBackendPipeline(RenderPipeline pipeline, Operation<CompiledRenderPipeline> original) {
        return original.call(SkyFarPlane.isDrawing() ? SkyFarPlane.far(pipeline) : pipeline);
    }

    @WrapOperation(
            method = "withRenderPass",
            at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Ljava/util/Optional;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/renderpearl/api/commands/RenderPass;"),
            require = 0
    )
    private static RenderPass polydium$hostNuitPass(CommandEncoder encoder, Supplier<String> label, GpuTextureView color, Optional<Vector4fc> clearColor, GpuTextureView depth, OptionalDouble clearDepth, Operation<RenderPass> original) {
        RenderPass host = SkyFarPlane.hostPass(color, depth);
        return host != null ? host : original.call(encoder, label, color, clearColor, depth, clearDepth);
    }

    @WrapOperation(method = "withRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;close()V"), require = 0)
    private static void polydium$keepNuitHostOpen(RenderPass pass, Operation<Void> original) {
        if (!SkyFarPlane.isHosting()) {
            original.call(pass);
        }
    }
    //?} else {
    /*@WrapOperation(
            method = "*",
            at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setPipeline(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)V"),
            require = 0
    )
    private static void polydium$farNuitBackendPipeline(RenderPass pass, RenderPipeline pipeline, Operation<Void> original) {
        original.call(pass, SkyFarPlane.isDrawing() ? SkyFarPlane.far(pipeline) : pipeline);
    }
    *///?}
}
//?}
