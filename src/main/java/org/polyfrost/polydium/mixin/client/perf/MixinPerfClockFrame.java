package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.renderer.GameRenderer;
import org.polyfrost.polydium.client.perf.PerfClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinPerfClockFrame {
    @Inject(method = "render", at = @At("HEAD"))
    private void polydium$advancePerfFrame(CallbackInfo ci) {
        PerfClock.frame++;
    }
}
