package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if < 1.21.5 {
/*import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderType.class)
public abstract class MixinGpuProfileLegacyRenderType extends RenderStateShard {
    private MixinGpuProfileLegacyRenderType(String name, Runnable setup, Runnable clear) {
        super(name, setup, clear);
    }

    @Inject(method = "draw", at = @At("HEAD"))
    private void polydium$profileLabel(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.legacyLabel(this.name);
    }

    @Inject(method = "draw", at = @At("RETURN"))
    private void polydium$profileLabelEnd(CallbackInfo ci) {
        GpuProfiler.legacyLabel(null);
    }
}
*///?}
