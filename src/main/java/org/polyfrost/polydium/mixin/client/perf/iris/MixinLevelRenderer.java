package org.polyfrost.polydium.mixin.client.perf.iris;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.uniforms.IrisTimeUniforms;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >= 1.21.4 {
import net.minecraft.util.profiling.Profiler;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Quaternionf;
*///?}
//? if >= 26.3 {
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
//?}

@Mixin(value = LevelRenderer.class, priority = 1500)
public class MixinLevelRenderer {
    private static final String IRIS_MIXIN = "net.irisshaders.iris.mixin.MixinLevelRenderer";
    private static final String IRIS_SKY_MIXIN = "net.irisshaders.iris.mixin.MixinLevelRenderer_Sky";

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$setupPipeline")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/uniforms/IrisTimeUniforms;updateTime()V"), require = 0, expect = 0)
    private boolean polydium$deferUpdateTime() {
        return false;
    }

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$setupPipeline")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/pipeline/PipelineManager;preparePipeline(Lnet/irisshaders/iris/shaderpack/materialmap/NamespacedId;)Lnet/irisshaders/iris/pipeline/WorldRenderingPipeline;"), require = 0, expect = 0)
    private WorldRenderingPipeline polydium$updateTimeWithPack(PipelineManager manager, NamespacedId dimension, Operation<WorldRenderingPipeline> original) {
        WorldRenderingPipeline pipeline = original.call(manager, dimension);
        if (pipeline instanceof IrisRenderingPipeline) IrisTimeUniforms.updateTime();
        return pipeline;
    }

    //? if >= 1.21.4 {
    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$beginLevelRender")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void polydium$skipEmptySetupPass(CallbackInfo ci) {
        if (!Iris.isPackInUseQuick()) ci.cancel();
    }
    //?}

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$endLevelRender")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"), require = 0, expect = 0)
    private void polydium$pushIrisFinal(ProfilerFiller profiler, String name, Operation<Void> original) {
        if (Iris.isPackInUseQuick()) profiler.push(name);
    }

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$endLevelRender")
    @Inject(method = "@MixinSquared:Handler", at = @At("TAIL"), require = 0, expect = 0)
    private void polydium$popIrisFinal(CallbackInfo ci) {
        if (Iris.isPackInUseQuick()) /*? if >= 1.21.4 {*/Profiler.get()/*?} else {*//*Minecraft.getInstance().getProfiler()*//*?}*/.pop();
    }

    //? if >= 26.3 {
    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$beginTranslucents")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void polydium$skipTranslucentSetup(LevelRenderer instance, ChunkSectionsToRender sections, FeatureRenderDispatcher.PreparedFrame frame, RenderPass renderPass, Operation<Void> original, CallbackInfo ci) {
        if (!Iris.isPackInUseQuick()) {
            original.call(instance, sections, frame, renderPass);
            ci.cancel();
        }
    }
    //?} else {
    /*@TargetHandler(mixin = IRIS_MIXIN, name = "iris$beginTranslucents")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void polydium$skipTranslucentSetup(CallbackInfo ci) {
        if (!Iris.isPackInUseQuick()) ci.cancel();
    }
    *///?}

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$beginTranslucents")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"), require = 0, expect = 0)
    private void polydium$pushPreTranslucent(ProfilerFiller profiler, String name, Operation<Void> original) {
        profiler.push(name);
    }

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$beginTranslucents")
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/pipeline/WorldRenderingPipeline;beginTranslucents()V", shift = At.Shift.AFTER), require = 0, expect = 0)
    private void polydium$popPreTranslucent(CallbackInfo ci) {
        /*? if >= 1.21.4 {*/Profiler.get()/*?} else {*//*Minecraft.getInstance().getProfiler()*//*?}*/.pop();
    }

    //? if >= 26.1 {
    //? if >= 26.2 {
    private static final String MAIN_CAMERA = "Lnet/minecraft/client/renderer/GameRenderer;mainCamera()Lnet/minecraft/client/Camera;";
    //?} else {
    /*private static final String MAIN_CAMERA = "Lnet/minecraft/client/renderer/GameRenderer;getMainCamera()Lnet/minecraft/client/Camera;";
    *///?}

    @TargetHandler(mixin = IRIS_SKY_MIXIN, name = "preRenderSky")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getFluidInCamera()Lnet/minecraft/world/level/material/FogType;"), require = 0, expect = 0)
    private static FogType polydium$extractedFogType(Camera camera, Operation<FogType> original) {
        return /*? if >= 26.2 {*/Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState/*?} else {*//*((net.irisshaders.iris.mixin.LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getLevelRenderState()*//*?}*/.cameraRenderState.fogType;
    }

    @TargetHandler(mixin = IRIS_SKY_MIXIN, name = "preRenderSky")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = MAIN_CAMERA), require = 0, expect = 0)
    private static Camera polydium$skipMainCamera(GameRenderer gameRenderer, Operation<Camera> original) {
        return null;
    }

    @TargetHandler(mixin = IRIS_SKY_MIXIN, name = "preRenderSky")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;position()Lnet/minecraft/world/phys/Vec3;"), require = 0, expect = 0)
    private static Vec3 polydium$skipCameraPosition(Camera camera, Operation<Vec3> original) {
        return null;
    }

    @TargetHandler(mixin = IRIS_SKY_MIXIN, name = "preRenderSky")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;entity()Lnet/minecraft/world/entity/Entity;"), require = 0, expect = 0)
    private static Entity polydium$skipCameraEntity(Camera camera, Operation<Entity> original) {
        return null;
    }
    //?}

    //? if < 1.21.4 {
    /*@TargetHandler(mixin = IRIS_MIXIN, name = "iris$renderSky$tiltSun")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;"), require = 0, expect = 0)
    private Quaternionf polydium$noZeroTiltQuaternion(Axis axis, float degrees, Operation<Quaternionf> original) {
        return degrees != 0 ? original.call(axis, degrees) : null;
    }

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$renderSky$tiltSun")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V"), require = 0, expect = 0)
    private boolean polydium$skipZeroTilt(PoseStack poseStack, Quaternionf rotation) {
        return rotation != null;
    }
    *///?}
}
