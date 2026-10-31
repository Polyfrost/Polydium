package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Minecraft.class)
public abstract class MixinFpsLimitMinecraft {
    //? if >= 26.1 {
    @ModifyConstant(method = "renderFrame", constant = @Constant(intValue = 260))
    //?} else {
    /*@ModifyConstant(method = "runTick", constant = @Constant(intValue = 260))
    *///?}
    private int polydium$unlimitedCutoff(int original) {
        return 1500;
    }
}
