package org.polyfrost.polydium.mixin.client.compat.mountopacity;

//? if >= 1.21.11 {
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.microcontrollers.mountopacity.hook.EntityHook", remap = false)
public abstract class MixinMountOpacityAlpha {
    @Shadow
    private static float entityOpacity;

    @Inject(method = "setOpacity(I)I", at = @At("HEAD"), cancellable = true, remap = false)
    private static void polydium$multiplyAlpha(int color, CallbackInfoReturnable<Integer> cir) {
        float opacity = entityOpacity;
        cir.setReturnValue(opacity < 0F || opacity == 100F ? color : ARGB.multiplyAlpha(color, opacity / 100F));
    }
}
//?}
