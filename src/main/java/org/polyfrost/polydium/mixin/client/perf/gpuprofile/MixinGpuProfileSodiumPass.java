package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if < 26.2 {
/*import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer", remap = false)
public abstract class MixinGpuProfileSodiumPass {
    @Unique
    private long polydium$outerLabels;

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void polydium$profileTerrainPass(CallbackInfo ci, @Local(argsOnly = true) TerrainRenderPass pass) {
        if (!GpuProfiler.recording)
            return;

        polydium$outerLabels = GpuProfiler.labels();
        GpuProfiler.pass("sodium terrain");
        GpuProfiler.pipeline(pass, p -> "sodium:terrain/" + (p.isTranslucent() ? "translucent" : p.supportsFragmentDiscard() ? "cutout" : "solid"));
    }

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    private void polydium$endTerrainPass(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.restoreLabels(polydium$outerLabels);
    }
}
*///?}
