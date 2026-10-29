package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4f;
import org.polyfrost.polydium.client.perf.OutlineScissor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
//?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.OutlineScissor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}
//? if >= 1.21.11 && < 26.2 {
/*import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
*///?} else if >= 1.21.5 && < 1.21.11 {
/*import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Final;
*///?} else if >= 1.21.4 && < 1.21.5 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.RenderType;
*///?} else if < 1.21.4 {
/*import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
*///?}

//? if >= 26.2 {
@Mixin(RenderType.class)
public abstract class MixinOutlineScissorRenderType {
    @Unique
    private static Matrix4f polydium$outlineModelView;

    @Shadow
    public abstract boolean isOutline();

    //? if >= 26.3 {
    @ModifyArg(method = "writeDynamicTransforms", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicGpuData;writeTransform(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;"), index = 0)
    //?} else {
    /*@ModifyArg(method = "writeDynamicTransforms", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;"), index = 0)
    *///?}
    private Matrix4f polydium$keepOutlineModelView(Matrix4f modelView) {
        if (this.isOutline())
            polydium$outlineModelView = modelView;
        return modelView;
    }

    @ModifyReturnValue(method = "prepare", at = @At("RETURN"))
    private PreparedRenderType polydium$attachOutlineModelView(PreparedRenderType prepared) {
        if (this.isOutline()) {
            ((OutlineScissor.ModelView) (Object) prepared).polydium$setModelView(polydium$outlineModelView);
            polydium$outlineModelView = null;
        }
        return prepared;
    }
}
//?} else if >= 1.21.11 {
/*@Mixin(RenderType.class)
public abstract class MixinOutlineScissorRenderType {
    @Shadow
    public abstract boolean isOutline();

    @Shadow
    public abstract RenderPipeline pipeline();

    @Inject(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/vertex/VertexFormat;uploadImmediateVertexBuffer(Ljava/nio/ByteBuffer;)Lcom/mojang/renderpearl/api/buffers/GpuBuffer;"))
    private void polydium$measureOutline(MeshData mesh, CallbackInfo ci) {
        if (PolydiumToggles.outlineScissor && this.isOutline()) {
            RenderPipeline pipeline = this.pipeline();
            OutlineScissor.measureDraw(pipeline == RenderPipelines.OUTLINE_CULL || pipeline == RenderPipelines.OUTLINE_NO_CULL, mesh, OutlineScissor.levelProjection(), RenderSystem.getModelViewMatrix());
        }
    }
}
*///?} else if >= 1.21.5 {
/*@Mixin(targets = "net.minecraft.client.renderer.RenderType$CompositeRenderType")
public abstract class MixinOutlineScissorRenderType {
    @Shadow
    @Final
    private RenderPipeline renderPipeline;

    @Shadow
    public abstract boolean isOutline();

    @Inject(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/vertex/VertexFormat;uploadImmediateVertexBuffer(Ljava/nio/ByteBuffer;)Lcom/mojang/renderpearl/api/buffers/GpuBuffer;"))
    private void polydium$measureOutline(MeshData mesh, CallbackInfo ci) {
        if (PolydiumToggles.outlineScissor && this.isOutline()) {
            RenderPipeline pipeline = this.renderPipeline;
            //? if >= 1.21.8 {
            OutlineScissor.measureDraw(pipeline == RenderPipelines.OUTLINE_CULL || pipeline == RenderPipelines.OUTLINE_NO_CULL, mesh, OutlineScissor.levelProjection(), RenderSystem.getModelViewMatrix());
            //?} else {
            /^OutlineScissor.measureDraw(pipeline == RenderPipelines.OUTLINE_CULL || pipeline == RenderPipelines.OUTLINE_NO_CULL, mesh, RenderSystem.getProjectionMatrix(), RenderSystem.getModelViewMatrix());
            ^///?}
        }
    }
}
*///?} else {
/*@Mixin(RenderType.class)
public abstract class MixinOutlineScissorRenderType {
    @Shadow
    public abstract boolean isOutline();

    @Inject(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"))
    private void polydium$measureOutline(MeshData mesh, CallbackInfo ci) {
        if (PolydiumToggles.outlineScissor && this.isOutline()) {
            //? if >= 1.21.4 {
            boolean vanillaShader = RenderSystem.getShader() == Minecraft.getInstance().getShaderManager().getProgram(CoreShaders.RENDERTYPE_OUTLINE);
            //?} else {
            /^boolean vanillaShader = RenderSystem.getShader() == GameRenderer.getRendertypeOutlineShader();
            ^///?}
            OutlineScissor.measureDraw(vanillaShader, mesh, RenderSystem.getProjectionMatrix(), RenderSystem.getModelViewMatrix());
        }
    }
}
*///?}
