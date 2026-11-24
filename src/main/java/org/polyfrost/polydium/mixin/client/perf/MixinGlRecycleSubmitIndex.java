package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import org.polyfrost.polydium.client.perf.RecyclePolling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public class MixinGlRecycleSubmitIndex {
    @Shadow
    private long currentSubmitIndex;

    @Inject(method = "submit", at = @At("TAIL"))
    private void polydium$recordSubmitIndex(CallbackInfo ci) {
        RecyclePolling.glSubmitIndex = this.currentSubmitIndex;
    }
}
//?}
