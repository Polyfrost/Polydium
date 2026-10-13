package org.polyfrost.polydium.mixin.client.perf.iris;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.VanillaRenderingPipeline", remap = false)
public class MixinVanillaRenderingPipeline {
    @WrapWithCondition(method = "beginLevelRendering", at = @At(value = "INVOKE", target = /*? if >= 1.21.5 {*/"Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_glUseProgram(I)V"/*?} else {*//*"Lcom/mojang/blaze3d/platform/GlStateManager;_glUseProgram(I)V"*//*?}*/), require = 0, expect = 0)
    private boolean polydium$skipUseProgram(int program) {
        return false;
    }
}
