package org.polyfrost.polydium.mixin.client.perf.overflow;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if >= 26.2 {
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

@Pseudo
@Mixin(targets = "org.polyfrost.overflowanimations.handler.rendering.LegacySkyRenderer", remap = false)
public class MixinLegacySkyRenderer {
    //? if >= 26.2 {
    @Inject(method = "renderBlueVoid", at = @At("HEAD"), require = 0)
    private static void polydium$blockUnpatchedBlueVoid(CallbackInfo ci) {
        if (!SkyFarPlane.isPatched(SkyFarPlane.OVERFLOW_SKY_SHADER)) {
            SkyFarPlane.blockNextDefer();
        }
    }
    //?}
}
