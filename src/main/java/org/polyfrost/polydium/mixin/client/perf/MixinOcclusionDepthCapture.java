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
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.polyfrost.polydium.client.perf.GpuWaitMeter;
import org.polyfrost.polydium.client.perf.MainPassSwap;
//?} else {
/*import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class MixinOcclusionDepthCapture {
    //? if >= 26.3 {
    @WrapOperation(method = "executeSolid", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeSolid(Lcom/mojang/renderpearl/api/commands/RenderPass;)V"))
    private void polydium$captureOpaqueDepth(FeatureRenderDispatcher.PreparedFrame frame, RenderPass pass, Operation<Void> original) {
        if (!OcclusionDepth.canCapture()) {
            original.call(frame, pass);
            return;
        }

        RenderTarget mainTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        long         start      = System.nanoTime();

        pass.close();

        long closed = System.nanoTime();

        OcclusionDepth.capture(mainTarget);

        long reopen = System.nanoTime();

        RenderPass features = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Main", mainTarget.getColorTextureView(), Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty());

        RenderSystem.bindDefaultUniforms(features);
        MainPassSwap.reopened(features);
        GpuWaitMeter.addCullCost(closed - start + System.nanoTime() - reopen);
        original.call(frame, features);
    }

    @WrapOperation(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;executeSolid(Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/renderpearl/api/commands/RenderPass;)V"))
    private void polydium$continueAfterCapture(LevelRenderer renderer, ChunkSectionsToRender sections, FeatureRenderDispatcher.PreparedFrame frame, RenderPass pass, Operation<Void> original, @Local LocalRef<RenderPass> mainPass) {
        original.call(renderer, sections, frame, pass);

        RenderPass reopened = MainPassSwap.take();

        if (reopened != null)
            mainPass.set(reopened);
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
