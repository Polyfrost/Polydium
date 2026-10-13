package org.polyfrost.polydium.mixin.client.perf.iris;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?}
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pathways.HandRenderer", remap = false)
public class MixinHandRenderer {
    @WrapMethod(method = {"renderSolid", "renderTranslucent"}, require = 0, expect = 0)
    private void polydium$skipWithoutPack(Matrix4fc modelMatrix, float tickDelta, Camera camera, /*? if >= 26.1 {*/CameraRenderState cameraState, /*?}*/GameRenderer gameRenderer,
                                          WorldRenderingPipeline pipeline, Operation<Void> original) {
        if (Iris.isPackInUseQuick()) {
            original.call(modelMatrix, tickDelta, camera, /*? if >= 26.1 {*/cameraState, /*?}*/gameRenderer, pipeline);
        }
    }
}
