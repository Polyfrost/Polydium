package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 1.21.11 {
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Identifier.class)
public abstract class MixinIrisUppercasePaths {
    @Inject(method = "isValidPath", at = @At("HEAD"), cancellable = true)
    private static void polydium$keepDummyInvalid(String path, CallbackInfoReturnable<Boolean> cir) {
        if ("DUMMY".equals(path)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "validPathChar", at = @At("HEAD"), cancellable = true)
    private static void polydium$allowUppercase(char c, CallbackInfoReturnable<Boolean> cir) {
        if (c >= 'A' && c <= 'Z') {
            cir.setReturnValue(true);
        }
    }
}
//?}
