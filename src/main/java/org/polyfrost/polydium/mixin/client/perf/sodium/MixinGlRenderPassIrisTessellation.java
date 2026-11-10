package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import org.polyfrost.polydium.client.perf.SodiumGlIndirect;
import org.polyfrost.polydium.compat.Mods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlRenderPass")
public abstract class MixinGlRenderPassIrisTessellation {
    @Unique
    private static final boolean polydium$IRIS = Mods.loaded("iris");

    @Shadow
    protected GlRenderPipeline pipeline;

    @Inject(method = "setPipeline", at = @At("TAIL"))
    private void polydium$trackTessellation(CallbackInfo ci) {
        SodiumGlIndirect.irisTessellation = polydium$IRIS && pipeline.program() instanceof ExtendedShader shader && shader.usesTessellation();
    }
}
//?}
