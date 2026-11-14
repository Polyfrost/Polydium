package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if >= 1.21.5 {
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if < 26.2 {
/*import com.llamalad7.mixinextras.sugar.Local;

import java.util.function.Supplier;
*///?}

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public abstract class MixinGpuProfileGlEncoder {
    @Inject(method = "createRenderPass", at = @At("HEAD"))
    private void polydium$profilePass(CallbackInfoReturnable<?> cir/*? if > 1.21.5 && < 26.2 {*//*, @Local(argsOnly = true) Supplier<String> label*//*?}*/) {
        if (!GpuProfiler.recording)
            return;

        //? if > 1.21.5 && < 26.2
        /*GpuProfiler.pass(label);*/
        GpuProfiler.beginPass(null);
    }

    @Inject(method = "createRenderPass", at = @At("RETURN"))
    private void polydium$profilePassEnd(CallbackInfoReturnable<?> cir) {
        if (GpuProfiler.recording)
            GpuProfiler.end(null);
    }

    //? if < 26.2 {
    /*@Inject(method = {"clearColorTexture", "clearColorAndDepthTextures", "clearDepthTexture"}, at = @At("HEAD"), require = 0)
    private void polydium$profileClear(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginOp(null, GpuProfiler.OP_CLEAR);
    }

    @Inject(method = {"writeToBuffer", "copyToBuffer", "writeToTexture", "copyBufferToTexture"}, at = @At("HEAD"), require = 0)
    private void polydium$profileUpload(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginOp(null, GpuProfiler.OP_UPLOAD);
    }

    @Inject(method = {"copyTextureToBuffer", "copyTextureToTexture"}, at = @At("HEAD"), require = 0)
    private void polydium$profileCopy(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginOp(null, GpuProfiler.OP_COPY);
    }

    @Inject(method = {"clearColorTexture", "clearColorAndDepthTextures", "clearDepthTexture", "writeToBuffer", "copyToBuffer", "writeToTexture",
        "copyBufferToTexture", "copyTextureToBuffer", "copyTextureToTexture"}, at = @At("RETURN"), require = 0)
    private void polydium$profileOpEnd(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(null);
    }
    *///?}

    @Inject(method = "presentTexture", at = @At("HEAD"), require = 0)
    private void polydium$profilePresent(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginOp(null, GpuProfiler.OP_PRESENT);
    }

    @Inject(method = "presentTexture", at = @At("RETURN"), require = 0)
    private void polydium$profilePresentEnd(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(null);
    }
}
//?}
