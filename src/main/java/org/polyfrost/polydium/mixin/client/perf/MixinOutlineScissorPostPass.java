package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostPass;
import org.polyfrost.polydium.client.perf.OutlineScissor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 1.21.5 {
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.commands.RenderPass;
//?} else if >= 1.21.4 {
/*import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
*///?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
*///?}

@Mixin(PostPass.class)
public abstract class MixinOutlineScissorPostPass {
    //? if >= 26.2 {
    @WrapOperation(method = "lambda$addToFrame$1", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;draw(IIII)V"))
    private void polydium$drawInsideOutline(RenderPass renderPass, int vertexCount, int instanceCount, int firstVertex, int firstInstance, Operation<Void> original, @Local RenderTarget outputTarget) {
        if (OutlineScissor.isOutlinePass((PostPass) (Object) this)) {
            if (OutlineScissor.isEmpty(outputTarget.width, outputTarget.height))
                return;
            OutlineScissor.enableScissor(renderPass, outputTarget.width, outputTarget.height);
        }
        original.call(renderPass, vertexCount, instanceCount, firstVertex, firstInstance);
    }
    //?} else if >= 1.21.10 {
    /*@WrapOperation(method = /^? if >= 26.1 {^/"lambda$addToFrame$1"/^?} else {^//^"method_67884"^//^?}^/, at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;draw(II)V"))
    private void polydium$drawInsideOutline(RenderPass renderPass, int firstVertex, int vertexCount, Operation<Void> original, @Local RenderTarget outputTarget) {
        if (OutlineScissor.isOutlinePass((PostPass) (Object) this)) {
            if (OutlineScissor.isEmpty(outputTarget.width, outputTarget.height))
                return;
            OutlineScissor.enableScissor(renderPass, outputTarget.width, outputTarget.height);
        }
        original.call(renderPass, firstVertex, vertexCount);
    }
    *///?} else if >= 1.21.8 {
    /*@WrapOperation(method = "method_67884", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;drawIndexed(IIII)V"))
    private void polydium$drawInsideOutline(RenderPass renderPass, int baseVertex, int firstIndex, int indexCount, int instanceCount, Operation<Void> original, @Local RenderTarget outputTarget) {
        if (OutlineScissor.isOutlinePass((PostPass) (Object) this)) {
            if (OutlineScissor.isEmpty(outputTarget.width, outputTarget.height))
                return;
            OutlineScissor.enableScissor(renderPass, outputTarget.width, outputTarget.height);
        }
        original.call(renderPass, baseVertex, firstIndex, indexCount, instanceCount);
    }
    *///?} else if >= 1.21.5 {
    /*@WrapOperation(method = "method_67884", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;drawIndexed(II)V"))
    private void polydium$drawInsideOutline(RenderPass renderPass, int firstIndex, int indexCount, Operation<Void> original, @Local RenderTarget outputTarget) {
        if (OutlineScissor.isOutlinePass((PostPass) (Object) this)) {
            if (OutlineScissor.isEmpty(outputTarget.width, outputTarget.height))
                return;
            OutlineScissor.enableScissor(renderPass, outputTarget.width, outputTarget.height);
        }
        original.call(renderPass, firstIndex, indexCount);
    }
    *///?} else if >= 1.21.4 {
    /*@WrapOperation(method = "method_62257", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"))
    private void polydium$drawInsideOutline(MeshData mesh, Operation<Void> original, @Local RenderTarget outputTarget) {
        boolean scissor = OutlineScissor.isOutlinePass((PostPass) (Object) this) && OutlineScissor.enableGlScissor(outputTarget.width, outputTarget.height);
        try {
            original.call(mesh);
        } finally {
            if (scissor)
                RenderSystem.disableScissor();
        }
    }
    *///?} else {
    /*@Shadow
    @Final
    public RenderTarget outTarget;

    @WrapOperation(method = "process", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;draw(Lcom/mojang/blaze3d/vertex/MeshData;)V"))
    private void polydium$drawInsideOutline(MeshData mesh, Operation<Void> original) {
        boolean scissor = OutlineScissor.isOutlinePass((PostPass) (Object) this) && OutlineScissor.enableGlScissor(this.outTarget.width, this.outTarget.height);
        try {
            original.call(mesh);
        } finally {
            if (scissor)
                RenderSystem.disableScissor();
        }
    }
    *///?}
}
