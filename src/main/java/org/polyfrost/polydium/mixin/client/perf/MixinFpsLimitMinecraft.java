package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.PolydiumConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MixinFpsLimitMinecraft {
    //~ if >= 26.1 'runTick' -> 'renderFrame'
    @ModifyExpressionValue(method = "renderFrame", at = @At(value = "CONSTANT", args = "intValue=260"))
    private int polydium$unlimitedCutoff(int original) {
        return PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF;
    }
}
