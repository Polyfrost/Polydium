package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = {
    "net.caffeinemc.mods.sodium.client.gui.SodiumConfigBuilder",
    "net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages"
}, remap = false)
public abstract class MixinFpsLimitSodium {
    @ModifyExpressionValue(method = "buildGeneralPage", at = @At(value = "CONSTANT", args = "intValue=260"), remap = false, require = 0)
    private int polydium$sliderMax(int original) {
        return 1500;
    }

    @ModifyExpressionValue(method = {"lambda$general$19", "lambda$general$20"}, at = @At(value = "CONSTANT", args = "intValue=260"), remap = false, require = 0)
    private static int polydium$sliderMaxLambda(int original) {
        return 1500;
    }
}
