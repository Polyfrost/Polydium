package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import org.polyfrost.polydium.client.perf.PresentThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {"org.jetbrains.skia.DirectContext", "org.jetbrains.skia.Surface"}, remap = false)
public abstract class MixinSkiaPresentDrain {

    @Inject(method = {"submit", "flushAndSubmit"}, at = @At("HEAD"), require = 0, remap = false)
    private void polydium$drainPresentThread(CallbackInfo ci) {
        PresentThread.await();
    }
}
//?}
