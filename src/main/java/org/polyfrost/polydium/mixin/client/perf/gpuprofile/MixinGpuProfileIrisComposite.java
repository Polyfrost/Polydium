package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.CompositeRenderer", remap = false)
public abstract class MixinGpuProfileIrisComposite {
    @Unique
    private long polydium$labels;

    @Inject(method = "renderAll", at = @At("HEAD"), require = 0)
    private void polydium$saveLabels(CallbackInfo ci) {
        if (GpuProfiler.recording)
            this.polydium$labels = GpuProfiler.labels();
    }

    @Inject(method = "renderAll", at = @At("RETURN"), require = 0)
    private void polydium$restoreLabels(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.restoreLabels(this.polydium$labels);
    }

    @WrapOperation(method = "renderAll", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/GLDebug;pushGroup(ILjava/lang/String;)V", ordinal = 1), require = 0)
    private void polydium$labelPass(int id, String name, Operation<Void> original) {
        original.call(id, name);

        if (GpuProfiler.recording)
            GpuProfiler.renamePass("Iris " + name);
    }

    //? if < 1.21.5 || >= 26.1 {
    @Inject(method = "renderAll", at = @At(value = "INVOKE", target = /*? if >= 26.1 {*/"Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_drawElements(IIIJ)V"/*?} else {*//*"Lnet/irisshaders/iris/pathways/FullScreenQuadRenderer;renderQuad()V"*//*?}*/), require = 0)
    private void polydium$profileDraw(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(null, "iris:composite", 1);
    }

    @Inject(method = "renderAll", at = @At(value = "INVOKE", target = /*? if >= 26.1 {*/"Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_drawElements(IIIJ)V"/*?} else {*//*"Lnet/irisshaders/iris/pathways/FullScreenQuadRenderer;renderQuad()V"*//*?}*/, shift = At.Shift.AFTER), require = 0)
    private void polydium$profileDrawEnd(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(null);
    }
    //?}
}
