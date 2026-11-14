package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if >= 26.2 {
import com.llamalad7.mixinextras.sugar.Local;
//? if >= 26.3 {
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
//?} else
/*import com.mojang.renderpearl.api.pipeline.RenderPipeline;*/
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import org.lwjgl.PointerBuffer;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;
import java.util.Collection;

@Mixin(targets = /*? if >= 26.3 {*/"com.mojang.renderpearl.frontend.FrontendRenderPass"/*?} else {*//*"com.mojang.renderpearl.api.commands.RenderPass"*//*?}*/)
public abstract class MixinGpuProfileFrontendPass {
    //? if >= 26.3 {
    @Inject(method = "setPipeline", at = @At("HEAD"))
    private void polydium$profilePipeline(CompiledRenderPipeline pipeline, CallbackInfo ci) {
        if (GpuProfiler.recording && pipeline instanceof FrontendRenderPipeline frontend)
            GpuProfiler.pipeline(frontend, FrontendRenderPipeline::name);
    }
    //?} else {
    /*@Inject(method = "setPipeline", at = @At("HEAD"))
    private void polydium$profilePipeline(RenderPipeline pipeline, CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.pipeline(pipeline, p -> p.getLocation().toString());
    }
    *///?}

    @Inject(method = {"drawIndexed", "draw"}, at = @At("HEAD"))
    private void polydium$profileDraw(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(this, 1);
    }

    @Inject(method = {"multiDrawIndexed(Ljava/nio/IntBuffer;III)V", "multiDraw(Ljava/nio/IntBuffer;III)V"}, at = @At("HEAD"))
    private void polydium$profileInterleavedDraws(IntBuffer commands, int stride, int firstInstance, int drawCount, CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(this, drawCount);
    }

    @Inject(method = "multiDrawIndexed(Lorg/lwjgl/PointerBuffer;Ljava/nio/IntBuffer;Ljava/nio/IntBuffer;I)V", at = @At("HEAD"))
    private void polydium$profileSeparateIndexedDraws(PointerBuffer offsets, IntBuffer counts, IntBuffer baseVertices, int drawCount, CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(this, drawCount);
    }

    @Inject(method = "multiDraw(Ljava/nio/IntBuffer;Ljava/nio/IntBuffer;I)V", at = @At("HEAD"))
    private void polydium$profileSeparateDraws(IntBuffer firsts, IntBuffer counts, int drawCount, CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(this, drawCount);
    }

    @Inject(method = {"drawIndexedIndirect", "drawIndirect"}, at = @At("HEAD"))
    private void polydium$profileIndirectDraws(GpuBufferSlice commands, int drawCount, CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(this, drawCount);
    }

    @Inject(method = "drawMultipleIndexed", at = @At("HEAD"))
    private void polydium$profileDraws(CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) Collection<?> draws) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(this, draws.size());
    }

    @Inject(method = {"drawIndexed", "multiDrawIndexed", "drawIndexedIndirect", "draw", "multiDraw", "drawIndirect", "drawMultipleIndexed"},
        at = @At("RETURN"), require = 0)
    private void polydium$profileDrawEnd(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(this);
    }
}
//?}
