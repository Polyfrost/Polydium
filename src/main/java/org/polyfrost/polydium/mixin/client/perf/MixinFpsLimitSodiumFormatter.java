package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.polyfrost.polydium.client.PolydiumConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls", remap = false)
public abstract class MixinFpsLimitSodiumFormatter {
    @ModifyExpressionValue(method = {"lambda$fpsLimit$0", "lambda$fpsLimit$2"}, at = @At(value = "CONSTANT", args = "intValue=260"), remap = false, require = 0)
    private static int polydium$unlimitedLabel(int original) {
        return PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF;
    }
}
