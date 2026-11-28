package org.polyfrost.polydium.mixin.client.compat.mountopacity;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "dev.microcontrollers.mountopacity.hook.EntityHook", remap = false)
public abstract class MixinMountOpacityAlpha {
    @WrapOperation(method = "setOpacity(I)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;colorFromFloat(FFFF)I", remap = true), remap = false, require = 0)
    private static int polydium$multiplyAlpha(float alpha, float red, float green, float blue, Operation<Integer> original, @Local(argsOnly = true) int color) {
        //? if >= 1.21.11 {
        return ARGB.multiplyAlpha(color, alpha);
        //?} else
        /*return ARGB.color(ARGB.alphaFloat(color) * alpha, color);*/
    }
}
//?}
