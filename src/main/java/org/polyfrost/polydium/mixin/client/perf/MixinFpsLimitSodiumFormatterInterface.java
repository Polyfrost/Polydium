package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter", remap = false)
public interface MixinFpsLimitSodiumFormatterInterface {
    @ModifyExpressionValue(method = "lambda$fpsLimit$2", at = @At(value = "CONSTANT", args = "intValue=260"), remap = false, require = 0)
    private static int polydium$unlimitedLabel(int original) {
        return 1500;
    }
}
