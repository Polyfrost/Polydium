package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.2 {
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gpu.device.context.VKIndirectContext", remap = false)
public abstract class MixinIndirectRingRotate {
    @Shadow
    private int currentOffset;

    // flawless frames (e.g. Flashback exports) rotate once per chunk update pass within a single submit,
    // which wraps the ring onto a fence for that same submit and throws; an untouched buffer has nothing to protect
    @Inject(method = "rotate", at = @At("HEAD"), cancellable = true)
    private void polydium$skipUnusedRotate(CallbackInfo ci) {
        if (this.currentOffset == 0) {
            ci.cancel();
        }
    }
}
//?}
