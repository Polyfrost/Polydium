package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Identifier.class)
public abstract class MixinIrisUppercasePaths {
    @ModifyReturnValue(method = "isValidPath", at = @At("RETURN"))
    private static boolean polydium$keepDummyInvalid(boolean valid, @Local(argsOnly = true) String path) {
        return valid && !"DUMMY".equals(path);
    }

    @ModifyReturnValue(method = "validPathChar", at = @At("RETURN"))
    private static boolean polydium$allowUppercase(boolean valid, @Local(argsOnly = true) char c) {
        return valid || c >= 'A' && c <= 'Z';
    }
}
//?}
