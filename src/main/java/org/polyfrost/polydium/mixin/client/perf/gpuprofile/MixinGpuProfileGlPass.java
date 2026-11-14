package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if >= 1.21.5 && < 26.2 {
/*import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlRenderPass")
public abstract class MixinGpuProfileGlPass {
    @Inject(method = "setPipeline", at = @At("HEAD"))
    private void polydium$profilePipeline(RenderPipeline pipeline, CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.pipeline(pipeline, p -> p.getLocation().toString());
    }

    @Inject(method = {"drawIndexed", "draw", "multiDrawIndexed", "drawIndexedIndirect", "multiDraw", "drawIndirect"}, at = @At("HEAD"), require = 0)
    private void polydium$profileDraw(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(null, 1);
    }

    @Inject(method = "drawMultipleIndexed", at = @At("HEAD"))
    private void polydium$profileDraws(CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) Collection<?> draws) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(null, draws.size());
    }

    @Inject(method = {"drawIndexed", "draw", "multiDrawIndexed", "drawIndexedIndirect", "multiDraw", "drawIndirect", "drawMultipleIndexed"}, at = @At("RETURN"), require = 0)
    private void polydium$profileDrawEnd(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(null);
    }
}
*///?}
