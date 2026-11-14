package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if < 26.2 {
/*import net.caffeinemc.mods.sodium.client.gl.device.MultiDrawBatch;
import net.caffeinemc.mods.sodium.client.gl.tessellation.GlIndexType;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gl.device.GLRenderDevice$ImmediateDrawCommandList", remap = false)
public abstract class MixinGpuProfileSodiumGl {
    @Inject(method = "multiDrawElementsBaseVertex", at = @At("HEAD"), require = 0)
    private void polydium$profileTerrain(MultiDrawBatch batch, GlIndexType indexType, CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.beginDraw(null, batch.size);
    }

    @Inject(method = "multiDrawElementsBaseVertex", at = @At("RETURN"), require = 0)
    private void polydium$profileTerrainEnd(MultiDrawBatch batch, GlIndexType indexType, CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(null);
    }
}
*///?}
