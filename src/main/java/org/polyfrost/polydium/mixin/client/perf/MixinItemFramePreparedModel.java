package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import org.polyfrost.polydium.client.perf.FrameModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemFrameRenderer.class)
public abstract class MixinItemFramePreparedModel {
    @WrapOperation(
        method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelResolver;updateForItemFrame(Lnet/minecraft/client/renderer/block/BlockModelRenderState;ZZ)V")
    )
    private void polydium$sharePreparedFrameModel(BlockModelResolver resolver, BlockModelRenderState renderState, boolean glowing, boolean map, Operation<Void> original, @Local(argsOnly = true) ItemFrameRenderState state) {
        BlockModelRenderState prepared = FrameModels.prepared(resolver, glowing, map);
        if (prepared != null) {
            ((ItemFrameRenderStateAccessor) state).polydium$setFrameModel(prepared);
        } else {
            original.call(resolver, polydium$own(state, renderState), glowing, map);
        }
    }

    @WrapOperation(
        method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelRenderState;clear()V")
    )
    private void polydium$clearOwnFrameModel(BlockModelRenderState renderState, Operation<Void> original, @Local(argsOnly = true) ItemFrameRenderState state) {
        original.call(polydium$own(state, renderState));
    }

    @WrapOperation(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelRenderState;submitWithZOffset(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V")
    )
    private void polydium$submitPreparedFrameModel(BlockModelRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, int outlineColor, Operation<Void> original) {
        FrameModels.Submission submission = FrameModels.submission(renderState);
        if (submission == null || !submission.submit(poseStack, collector, lightCoords, overlayCoords, outlineColor)) {
            original.call(renderState, poseStack, collector, lightCoords, overlayCoords, outlineColor);
        }
    }

    @Unique
    private static BlockModelRenderState polydium$own(ItemFrameRenderState state, BlockModelRenderState renderState) {
        if (!FrameModels.isPrepared(renderState)) {
            return renderState;
        }
        BlockModelRenderState own = new BlockModelRenderState();
        ((ItemFrameRenderStateAccessor) state).polydium$setFrameModel(own);
        return own;
    }
}
//?}
