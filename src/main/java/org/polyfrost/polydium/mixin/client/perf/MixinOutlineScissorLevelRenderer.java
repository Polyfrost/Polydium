//~ identifier
package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import org.polyfrost.polydium.client.perf.OutlineScissor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >= 1.21.4 {
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import java.util.Set;
//?} else {
/*import org.spongepowered.asm.mixin.Shadow;
*///?}
//? if >= 1.21.8 {
import com.mojang.renderpearl.api.textures.GpuTextureView;
//?} else if >= 1.21.5 {
/*import com.mojang.renderpearl.api.textures.GpuTexture;
*///?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}

@Mixin(LevelRenderer.class)
public abstract class MixinOutlineScissorLevelRenderer {
    //? if >= 26.2 {
    @Inject(method = "render", at = @At("HEAD"))
    private void polydium$beginOutlineFrame(CallbackInfo ci) {
        OutlineScissor.beginFrame();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain polydium$trackOutlineChain(ShaderManager shaderManager, Identifier id, Set<Identifier> targets, Operation<PostChain> original) {
        PostChain chain = original.call(shaderManager, id, targets);
        OutlineScissor.useChain(chain);
        return chain;
    }
    //?} else if >= 1.21.4 {
    /*@Inject(method = "renderLevel", at = @At("HEAD"))
    private void polydium$beginOutlineFrame(CallbackInfo ci) {
        OutlineScissor.beginFrame();
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain polydium$trackOutlineChain(ShaderManager shaderManager, Identifier id, Set<Identifier> targets, Operation<PostChain> original) {
        PostChain chain = original.call(shaderManager, id, targets);
        OutlineScissor.useChain(chain);
        return chain;
    }
    *///?} else {
    /*@Shadow
    private PostChain entityEffect;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void polydium$beginOutlineFrame(CallbackInfo ci) {
        OutlineScissor.useChain(this.entityEffect);
        OutlineScissor.beginFrame();
    }
    *///?}

    //? if >= 26.3 {
    @WrapOperation(method = "blitEntityOutline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitAndBlendToTexture(Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuTextureView;)V"))
    private void polydium$blendInsideOutline(RenderTarget target, GpuTextureView color, GpuTextureView depth, Operation<Void> original) {
        OutlineScissor.beginBlit();
        try {
            original.call(target, color, depth);
        } finally {
            OutlineScissor.endBlit();
        }
    }
    //?} else if >= 26.2 {
    /*@WrapOperation(method = "doEntityOutline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitAndBlendToTexture(Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuTextureView;)V"))
    private void polydium$blendInsideOutline(RenderTarget target, GpuTextureView color, GpuTextureView depth, Operation<Void> original) {
        OutlineScissor.beginBlit();
        try {
            original.call(target, color, depth);
        } finally {
            OutlineScissor.endBlit();
        }
    }
    *///?} else if >= 1.21.8 {
    /*@WrapOperation(method = "doEntityOutline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitAndBlendToTexture(Lcom/mojang/renderpearl/api/textures/GpuTextureView;)V"))
    private void polydium$blendInsideOutline(RenderTarget target, GpuTextureView output, Operation<Void> original) {
        OutlineScissor.beginBlit();
        try {
            original.call(target, output);
        } finally {
            OutlineScissor.endBlit();
        }
    }
    *///?} else if >= 1.21.5 {
    /*@WrapOperation(method = "doEntityOutline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitAndBlendToTexture(Lcom/mojang/renderpearl/api/textures/GpuTexture;)V"))
    private void polydium$blendInsideOutline(RenderTarget target, GpuTexture output, Operation<Void> original) {
        OutlineScissor.beginBlit();
        try {
            original.call(target, output);
        } finally {
            OutlineScissor.endBlit();
        }
    }
    *///?} else if >= 1.21.4 {
    /*@WrapOperation(method = "doEntityOutline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitAndBlendToScreen(II)V"))
    private void polydium$blendInsideOutline(RenderTarget target, int width, int height, Operation<Void> original) {
        OutlineScissor.beginBlit();
        boolean scissor = OutlineScissor.isBlitting() && OutlineScissor.enableGlScissor(target.width, target.height);
        try {
            original.call(target, width, height);
        } finally {
            if (scissor)
                RenderSystem.disableScissor();
            OutlineScissor.endBlit();
        }
    }
    *///?} else {
    /*@WrapOperation(method = "doEntityOutline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(IIZ)V"))
    private void polydium$blendInsideOutline(RenderTarget target, int width, int height, boolean disableBlend, Operation<Void> original) {
        OutlineScissor.beginBlit();
        boolean scissor = OutlineScissor.isBlitting() && OutlineScissor.enableGlScissor(target.width, target.height);
        try {
            original.call(target, width, height, disableBlend);
        } finally {
            if (scissor)
                RenderSystem.disableScissor();
            OutlineScissor.endBlit();
        }
    }
    *///?}
}
