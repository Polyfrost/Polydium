package org.polyfrost.polydium.mixin.client.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls", remap = false)
public abstract class MixinFpsLimitSodiumFormatter {
    @ModifyConstant(method = {"lambda$fpsLimit$0", "lambda$fpsLimit$2"}, constant = @Constant(intValue = 260), remap = false, require = 0)
    private static int polydium$unlimitedLabel(int original) {
        return 1500;
    }
}
