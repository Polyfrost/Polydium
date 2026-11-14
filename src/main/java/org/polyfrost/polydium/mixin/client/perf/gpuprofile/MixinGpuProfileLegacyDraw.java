package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if < 1.21.5 {
/*import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if < 1.21.4 {
import net.minecraft.client.renderer.ShaderInstance;
//?}

@Mixin(GlStateManager.class)
public abstract class MixinGpuProfileLegacyDraw {
    @Inject(method = "_drawElements", at = @At("HEAD"))
    private static void polydium$profileDraw(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.begin(null, GpuProfiler.key(GpuProfiler.NO_PASS, GpuProfiler.label(polydium$label())), 1);
    }

    @Inject(method = "_drawElements", at = @At("RETURN"))
    private static void polydium$profileDrawEnd(CallbackInfo ci) {
        if (GpuProfiler.recording)
            GpuProfiler.end(null);
    }

    private static String polydium$label() {
        String label = GpuProfiler.legacyLabel();

        if (label != null)
            return label;

        //? if < 1.21.4 {
        ShaderInstance shader = RenderSystem.getShader();

        return shader == null ? "(no shader)" : "shader " + shader.getName();
        //?} else
        /^return GpuProfiler.shaderName(RenderSystem.getShader());^/
    }
}
*///?}
