package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
//? if < 26.2 {
/*import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.item.ItemStackRenderState;
*///?}
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFeatureRenderer.class)
public abstract class MixinItemVertexBuilderReuse {
    @Unique
    private /*? if >= 26.2 {*/ItemFeatureRenderer.Submit/*?} else {*//*SubmitNodeStorage.ItemSubmit*//*?}*/ polydium$lastSubmit;
    @Unique
    private RenderType polydium$lastRenderType;
    @Unique
    private VertexConsumer polydium$lastVertexBuilder;

    //? if >= 26.2 {
    @WrapOperation(
            method = "prepareMainSubmit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;getVertexBuilder(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer polydium$reuseVertexBuilder(ItemFeatureRenderer renderer, RenderType renderType, Operation<VertexConsumer> original,
                                                       @Local(argsOnly = true) ItemFeatureRenderer.Submit submit) {
        if (submit == this.polydium$lastSubmit && renderType == this.polydium$lastRenderType && renderType.canConsolidateConsecutiveGeometry()) {
            return this.polydium$lastVertexBuilder;
        }
        VertexConsumer builder = original.call(renderer, renderType);
        this.polydium$lastSubmit = submit;
        this.polydium$lastRenderType = renderType;
        this.polydium$lastVertexBuilder = builder;
        return builder;
    }
    //?} else {
    /*@WrapOperation(
            method = "renderItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;getBuffer(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer polydium$reuseVertexBuilder(MultiBufferSource.BufferSource bufferSource, RenderType renderType, Operation<VertexConsumer> original,
                                                       @Local(argsOnly = true) SubmitNodeStorage.ItemSubmit submit) {
        if (submit.foilType() != ItemStackRenderState.FoilType.NONE) {
            return original.call(bufferSource, renderType);
        }
        if (submit == this.polydium$lastSubmit && renderType == this.polydium$lastRenderType && renderType.canConsolidateConsecutiveGeometry()) {
            return this.polydium$lastVertexBuilder;
        }
        VertexConsumer builder = original.call(bufferSource, renderType);
        this.polydium$lastSubmit = submit;
        this.polydium$lastRenderType = renderType;
        this.polydium$lastVertexBuilder = builder;
        return builder;
    }
    *///?}

    @Inject(method = /*? if >= 26.2 {*/"buildGroup"/*?} else {*//*"renderItem"*//*?}*/, at = {@At("HEAD"), @At("RETURN")})
    private void polydium$forgetVertexBuilder(CallbackInfo ci) {
        this.polydium$lastSubmit = null;
        this.polydium$lastRenderType = null;
        this.polydium$lastVertexBuilder = null;
    }
}
//?}
