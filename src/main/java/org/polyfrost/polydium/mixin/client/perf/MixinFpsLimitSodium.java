package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.SodiumConfigBuilder", remap = false)
public abstract class MixinFpsLimitSodium {
    @ModifyConstant(method = "buildGeneralPage", constant = @Constant(intValue = 260), remap = false, require = 0)
    private int polydium$sliderMax(int original) {
        return 1500;
    }
}
//?}
