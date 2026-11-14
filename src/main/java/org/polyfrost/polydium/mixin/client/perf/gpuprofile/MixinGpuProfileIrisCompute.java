package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.gl.program.ComputeProgram", remap = false)
public abstract class MixinGpuProfileIrisCompute {
    @Inject(method = "dispatch(FF)V", at = @At("HEAD"), require = 0)
    private void polydium$profileDispatch(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(null, "iris:compute", 0);
    }

    @Inject(method = "dispatch(FF)V", at = @At("RETURN"), require = 0)
    private void polydium$profileDispatchEnd(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(null);
    }
}
