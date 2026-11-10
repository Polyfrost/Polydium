package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.polyfrost.polydium.client.perf.OutlineScissor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
//?} else if >= 1.21.4 {
/*import org.spongepowered.asm.mixin.injection.ModifyArg;
*///?} else {
/*import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.injection.ModifyArg;
*///?}

import java.util.List;

@Mixin(PostChain.class)
public abstract class MixinOutlineScissorPostChain implements OutlineScissor.Passes {
    @Shadow
    @Final
    private List<PostPass> passes;

    @Override
    public List<PostPass> polydium$passes() {
        return this.passes;
    }

    //? if >= 26.3 {
    @ModifyExpressionValue(method = "addToFrame", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/resource/RenderTargetDescriptor$TextureProperties;DEFAULT_DEPTH:Lcom/mojang/blaze3d/resource/RenderTargetDescriptor$TextureProperties;"))
    private RenderTargetDescriptor.TextureProperties polydium$dropOutlineDepth(RenderTargetDescriptor.TextureProperties depth) {
        return OutlineScissor.isOutlineChain((PostChain) (Object) this) ? null : depth;
    }
    //?} else if >= 26.2 {
    /*@ModifyArg(method = "addToFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/resource/RenderTargetDescriptor;<init>(IIZLorg/joml/Vector4fc;Lcom/mojang/renderpearl/api/GpuFormat;)V"), index = 2)
    private boolean polydium$dropOutlineDepth(boolean useDepth) {
        return useDepth && !OutlineScissor.isOutlineChain((PostChain) (Object) this);
    }
    *///?} else if >= 1.21.5 {
    /*@ModifyArg(method = "addToFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/resource/RenderTargetDescriptor;<init>(IIZI)V"), index = 2)
    private boolean polydium$dropOutlineDepth(boolean useDepth) {
        return useDepth && !OutlineScissor.isOutlineChain((PostChain) (Object) this);
    }
    *///?} else if >= 1.21.4 {
    /*@ModifyArg(method = "addToFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/resource/RenderTargetDescriptor;<init>(IIZ)V"), index = 2)
    private boolean polydium$dropOutlineDepth(boolean useDepth) {
        return useDepth && !OutlineScissor.isOutlineChain((PostChain) (Object) this);
    }
    *///?} else {
    /*@Shadow
    @Final
    private String name;

    @ModifyArg(method = "addTempTarget", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/TextureTarget;<init>(IIZZ)V"), index = 2)
    private boolean polydium$dropOutlineDepth(boolean useDepth, @Local(argsOnly = true) String target) {
        return useDepth && !OutlineScissor.dropsDepth(this.name, target);
    }
    *///?}
}
