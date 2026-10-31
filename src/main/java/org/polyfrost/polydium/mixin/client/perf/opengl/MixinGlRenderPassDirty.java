package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 1.21.8 && < 26.3 {
/*import org.polyfrost.polydium.client.perf.GlPassDirty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Set;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlRenderPass")
public abstract class MixinGlRenderPassDirty {
    @Shadow
    @Final
    protected Set<String> dirtyUniforms;

    @Shadow
    @Final
    protected HashMap<String, ?> uniforms;

    @Inject(method = "drawMultipleIndexed", at = @At("RETURN"))
    private void polydium$invalidateBindings(CallbackInfo ci) {
        this.dirtyUniforms.addAll(this.uniforms.keySet());
    }

    @Inject(method = {"draw", "drawIndexed", "drawMultipleIndexed"/^? if >= 26.2 {^/, "drawIndirect", "drawIndexedIndirect", "multiDraw*", "multiDrawIndexed*"/^?}^/}, at = @At("HEAD"))
    private void polydium$markDrawing(CallbackInfo ci) {
        GlPassDirty.drawing = this.dirtyUniforms;
    }
}
*///?}
