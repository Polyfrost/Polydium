package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if >= 1.21.11 {
//? if >= 26.2 && < 26.3
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;*/
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if < 26.2 {
/*import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FramePass;
*///?}
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
//? if < 26.3
/*import net.minecraft.client.renderer.LevelTargetBundle;*/
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.SkyRenderState;
//?} else {
/*import net.minecraft.client.renderer.state.SkyRenderState;
*///?}
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.OptionalDouble;

@Mixin(LevelRenderer.class)
public abstract class MixinDeferredSky {
    //? if >= 26.3 {
    @Shadow @Final
    private GameRenderer gameRenderer;

    @Unique
    private RenderPass polydium$afterSkyPass;

    @WrapOperation(
            method = "lambda$addSkyPass$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;render(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lnet/minecraft/client/renderer/state/level/SkyRenderState;)V")
    )
    private void polydium$deferSky(SkyRenderer renderer, GpuBufferSlice fog, SkyRenderState state, Operation<Void> original) {
        if (PolydiumToggles.deferredSky && SkyFarPlane.canDefer(state, this.gameRenderer.useImprovedTransparency())) {
            SkyFarPlane.defer(renderer, fog, state);
        } else {
            original.call(renderer, fog, state);
        }
    }

    @WrapOperation(
            method = "executeSolid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeSolid(Lcom/mojang/renderpearl/api/commands/RenderPass;)V")
    )
    private void polydium$drawSkyAfterTerrain(FeatureRenderDispatcher.PreparedFrame frame, RenderPass pass, Operation<Void> original) {
        if (!SkyFarPlane.hasDeferred()) {
            original.call(frame, pass);
            return;
        }
        if (this.polydium$afterSkyPass != null) {
            this.polydium$afterSkyPass.close();
        }
        RenderTarget mainTarget = this.gameRenderer.mainRenderTarget();
        pass.close();
        SkyFarPlane.drawDeferred();
        RenderPass afterSky = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Main", mainTarget.getColorTextureView(), Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty());
        RenderSystem.bindDefaultUniforms(afterSky);
        this.polydium$afterSkyPass = afterSky;
        original.call(frame, afterSky);
    }

    @WrapOperation(
            method = "lambda$addMainPass$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;executeClassicTransparency(Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/renderpearl/api/commands/RenderPass;)V")
    )
    private void polydium$continueAfterSky(LevelRenderer renderer, ChunkSectionsToRender sections, FeatureRenderDispatcher.PreparedFrame frame, RenderPass pass, Operation<Void> original) {
        RenderPass afterSky = this.polydium$afterSkyPass;
        if (afterSky == null) {
            original.call(renderer, sections, frame, pass);
            return;
        }
        this.polydium$afterSkyPass = null;
        try (afterSky) {
            original.call(renderer, sections, frame, afterSky);
        }
    }
    //?} else if >= 26.2 {
    /*@Shadow @Final
    private LevelTargetBundle targets;

    @WrapMethod(method = "lambda$addSkyPass$0")
    private void polydium$deferSky(GpuBufferSlice fog, SkyRenderState state, Operation<Void> original) {
        if (PolydiumToggles.deferredSky && SkyFarPlane.canDefer(state, this.targets.translucent != null)) {
            SkyFarPlane.defer(() -> original.call(fog, state));
        } else {
            original.call(fog, state);
        }
    }

    @WrapOperation(
            method = "lambda$addMainPass$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeSolid()V")
    )
    private void polydium$drawSkyAfterTerrain(FeatureRenderDispatcher.PreparedFrame frame, Operation<Void> original) {
        if (SkyFarPlane.hasDeferred()) {
            SkyFarPlane.drawDeferred();
        }
        original.call(frame);
    }
    *///?} else {
    /*@Shadow @Final
    private LevelTargetBundle targets;

    @WrapOperation(method = "addSkyPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V"))
    private void polydium$deferSky(FramePass pass, Runnable sky, Operation<Void> original, @Local SkyRenderState state) {
        original.call(pass, (Runnable) () -> {
            if (PolydiumToggles.deferredSky && SkyFarPlane.canDefer(state, this.targets.translucent != null)) {
                SkyFarPlane.defer(sky);
            } else {
                sky.run();
            }
        });
    }

    @WrapOperation(
            method = /^? if >= 26.1 {^/"lambda$addMainPass$0"/^?} else {^//^"method_62214"^//^?}^/,
            at = @At(value = "INVOKE", target = /^? if >= 26.1 {^/"Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderSolidFeatures()V"/^?} else {^//^"Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V"^//^?}^/)
    )
    private void polydium$drawSkyAfterTerrain(FeatureRenderDispatcher dispatcher, Operation<Void> original) {
        if (SkyFarPlane.hasDeferred()) {
            SkyFarPlane.drawDeferred();
        }
        original.call(dispatcher);
    }
    *///?}
}
//?}
