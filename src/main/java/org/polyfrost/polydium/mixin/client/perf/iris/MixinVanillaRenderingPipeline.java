package org.polyfrost.polydium.mixin.client.perf.iris;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.VanillaRenderingPipeline", remap = false)
public class MixinVanillaRenderingPipeline {
    //? if >= 1.21.5 {
    @Inject(method = "beginLevelRendering", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void polydium$skipUseProgram(CallbackInfo ci) {
        ci.cancel();
    }
    //?} else {
    /*
    @WrapWithCondition(method = "beginLevelRendering", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glUseProgram(I)V", remap = true), require = 0, expect = 0)
    private boolean polydium$skipUseProgram(int program) {
        return false;
    }
    *///?}
}
