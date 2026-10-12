package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.polyfrost.polydium.client.perf.OcclusionDepth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
//?} else {
/*import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class MixinOcclusionDepthCapture {
    //? if >= 26.3 {
    @WrapOperation(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;executeClassicTransparency(Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/renderpearl/api/commands/RenderPass;)V"))
    private void polydium$captureOpaqueDepth(LevelRenderer renderer, ChunkSectionsToRender sections, FeatureRenderDispatcher.PreparedFrame frame, RenderPass pass, Operation<Void> original) {
        if (!OcclusionDepth.canCapture()) {
            original.call(renderer, sections, frame, pass);
            return;
        }

        RenderTarget mainTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();

        pass.close();
        OcclusionDepth.capture(mainTarget);

        try (RenderPass translucent = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Main", mainTarget.getColorTextureView(), Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(translucent);
            original.call(renderer, sections, frame, translucent);
        }
    }
    //?} else if >= 26.2 {
    /*@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeSolid()V"))
    private void polydium$captureOpaqueDepth(CallbackInfo ci) {
        if (OcclusionDepth.canCapture()) {
            OcclusionDepth.capture(Minecraft.getInstance().gameRenderer.mainRenderTarget());
        }
    }
    *///?} else if >= 26.1 {
    /*@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderSolidFeatures()V"))
    private void polydium$captureOpaqueDepth(CallbackInfo ci) {
        if (OcclusionDepth.canCapture()) {
            OcclusionDepth.capture(Minecraft.getInstance().getMainRenderTarget());
        }
    }
    *///?} else if >= 1.21.10 {
    /*@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V"))
    private void polydium$captureOpaqueDepth(CallbackInfo ci) {
        if (OcclusionDepth.canCapture()) {
            OcclusionDepth.capture(Minecraft.getInstance().getMainRenderTarget());
        }
    }
    *///?} else {
    /*@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/Camera;Lnet/minecraft/client/DeltaTracker;Ljava/util/List;)V"))
    private void polydium$captureOpaqueDepth(CallbackInfo ci) {
        if (OcclusionDepth.canCapture()) {
            OcclusionDepth.capture(Minecraft.getInstance().getMainRenderTarget());
        }
    }
    *///?}
}
//?}
