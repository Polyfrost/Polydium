package org.polyfrost.polydium.mixin.client.perf.iris;

//? if >= 1.21.4 {
import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?}
//? if >= 26.3 {
import com.mojang.renderpearl.api.commands.RenderPass;
import org.joml.Vector4fc;
//?} else {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
*///?}

@Mixin(value = SkyRenderer.class, priority = 1500)
public class MixinSkyRenderer {
    private static final String IRIS_SKY_MIXIN = "net.irisshaders.iris.mixin.MixinSkyRenderer";

    //? if >= 26.3 {
    @TargetHandler(mixin = IRIS_SKY_MIXIN, name = "iris$renderSky$tiltSun")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotateDegrees(Lcom/mojang/math/Axis;F)V"), require = 0, expect = 0)
    private boolean polydium$skipZeroTilt(PoseStack poseStack, Axis axis, float degrees) {
        return degrees != 0;
    }
    //?} else {
    /*@TargetHandler(mixin = IRIS_SKY_MIXIN, name = "iris$renderSky$tiltSun")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;"), require = 0, expect = 0)
    private Quaternionf polydium$noZeroTiltQuaternion(Axis axis, float degrees, Operation<Quaternionf> original) {
        return degrees != 0 ? original.call(axis, degrees) : null;
    }
    *///?}

    //? if >= 1.21.5 && < 26.3 {
    /*@TargetHandler(mixin = IRIS_SKY_MIXIN, name = "iris$renderSky$tiltSun")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V"), require = 0, expect = 0)
    private boolean polydium$skipZeroTilt(PoseStack poseStack, Quaternionfc rotation) {
        return rotation != null;
    }
    *///?} else if < 1.21.5 {
    /*@TargetHandler(mixin = IRIS_SKY_MIXIN, name = "iris$renderSky$tiltSun")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V"), require = 0, expect = 0)
    private boolean polydium$skipZeroTilt(PoseStack poseStack, Quaternionf rotation) {
        return rotation != null;
    }
    *///?}

    //? if >= 26.1 {
    @TargetHandler(mixin = "net.irisshaders.iris.mixin.sky.MixinDimensionSpecialEffects", name = "iris$getSunriseColor")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    //? if >= 26.3 {
    private void polydium$sunriseCancel(RenderPass renderPass, PoseStack poseStack, float sunAngle, Vector4fc color, CallbackInfo irisCi, CallbackInfo ci) {
    //?} else {
    /*private void polydium$sunriseCancel(PoseStack poseStack, float sunAngle, int color, CallbackInfo irisCi, CallbackInfo ci) {
    *///?}
        CameraRenderState cameraState = /*? if >= 26.2 {*/Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState/*?} else {*//*((net.irisshaders.iris.mixin.LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getLevelRenderState()*//*?}*/.cameraRenderState;
        if (cameraState.entityRenderState.doesMobEffectBlockSky || cameraState.fogType != FogType.NONE) {
            irisCi.cancel();
        }
        ci.cancel();
    }
    //?}
}
//?}
