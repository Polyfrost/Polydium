package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import org.joml.Vector4fc;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
//?}
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.SkyRenderer;
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public abstract class MixinSkyFarPlane {
    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderPipelines;SKY:Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"))
    private RenderPipeline polydium$farSky(RenderPipeline pipeline) {
        return SkyFarPlane.isDrawing() ? SkyFarPlane.Pipelines.SKY : pipeline;
    }

    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderPipelines;SUNRISE_SUNSET:Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"))
    private RenderPipeline polydium$farSunrise(RenderPipeline pipeline) {
        return SkyFarPlane.isDrawing() ? SkyFarPlane.Pipelines.SUNRISE_SUNSET : pipeline;
    }

    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderPipelines;STARS:Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"))
    private RenderPipeline polydium$farStars(RenderPipeline pipeline) {
        return SkyFarPlane.isDrawing() ? SkyFarPlane.Pipelines.STARS : pipeline;
    }

    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderPipelines;CELESTIAL:Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"))
    private RenderPipeline polydium$farCelestial(RenderPipeline pipeline) {
        return SkyFarPlane.isDrawing() ? SkyFarPlane.Pipelines.CELESTIAL : pipeline;
    }

    //? if >= 26.3 {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Ljava/util/Optional;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/renderpearl/api/commands/RenderPass;"))
    private RenderPass polydium$drawInMainPass(CommandEncoder encoder, Supplier<String> label, GpuTextureView color, Optional<Vector4fc> clearColor, GpuTextureView depth, OptionalDouble clearDepth, Operation<RenderPass> original) {
        RenderPass host = SkyFarPlane.hostPass(color, depth);
        return host != null ? host : original.call(encoder, label, color, clearColor, depth, clearDepth);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;close()V"))
    private void polydium$keepMainPassOpen(RenderPass pass, Operation<Void> original) {
        if (!SkyFarPlane.isHosting()) {
            original.call(pass);
        }
    }
    //?}
}
//?}
