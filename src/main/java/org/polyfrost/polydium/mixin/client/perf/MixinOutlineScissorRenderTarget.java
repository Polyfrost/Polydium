package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.5 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.commands.RenderPass;
import org.polyfrost.polydium.client.perf.OutlineScissor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderTarget.class)
public abstract class MixinOutlineScissorRenderTarget {
    //? if >= 26.2 {
    @WrapOperation(method = "blitAndBlendToTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;draw(IIII)V"))
    private void polydium$blendInsideOutline(RenderPass renderPass, int vertexCount, int instanceCount, int firstVertex, int firstInstance, Operation<Void> original) {
        if (OutlineScissor.isBlitting()) {
            RenderTarget target = (RenderTarget) (Object) this;
            if (OutlineScissor.isEmpty(target.width, target.height))
                return;
            OutlineScissor.enableScissor(renderPass, target.width, target.height);
        }
        original.call(renderPass, vertexCount, instanceCount, firstVertex, firstInstance);
    }
    //?} else if >= 1.21.10 {
    /*@WrapOperation(method = "blitAndBlendToTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;draw(II)V"))
    private void polydium$blendInsideOutline(RenderPass renderPass, int firstVertex, int vertexCount, Operation<Void> original) {
        if (OutlineScissor.isBlitting()) {
            RenderTarget target = (RenderTarget) (Object) this;
            if (OutlineScissor.isEmpty(target.width, target.height))
                return;
            OutlineScissor.enableScissor(renderPass, target.width, target.height);
        }
        original.call(renderPass, firstVertex, vertexCount);
    }
    *///?} else if >= 1.21.8 {
    /*@WrapOperation(method = "blitAndBlendToTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;drawIndexed(IIII)V"))
    private void polydium$blendInsideOutline(RenderPass renderPass, int baseVertex, int firstIndex, int indexCount, int instanceCount, Operation<Void> original) {
        if (OutlineScissor.isBlitting()) {
            RenderTarget target = (RenderTarget) (Object) this;
            if (OutlineScissor.isEmpty(target.width, target.height))
                return;
            OutlineScissor.enableScissor(renderPass, target.width, target.height);
        }
        original.call(renderPass, baseVertex, firstIndex, indexCount, instanceCount);
    }
    *///?} else {
    /*@WrapOperation(method = "blitAndBlendToTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;drawIndexed(II)V"))
    private void polydium$blendInsideOutline(RenderPass renderPass, int firstIndex, int indexCount, Operation<Void> original) {
        if (OutlineScissor.isBlitting()) {
            RenderTarget target = (RenderTarget) (Object) this;
            if (OutlineScissor.isEmpty(target.width, target.height))
                return;
            OutlineScissor.enableScissor(renderPass, target.width, target.height);
        }
        original.call(renderPass, firstIndex, indexCount);
    }
    *///?}
}
//?}
