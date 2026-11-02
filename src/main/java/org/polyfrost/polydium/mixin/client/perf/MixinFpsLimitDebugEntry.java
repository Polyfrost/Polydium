package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.debug.DebugEntryFps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugEntryFps.class)
public abstract class MixinFpsLimitDebugEntry {
    @ModifyExpressionValue(method = "display", at = @At(value = "CONSTANT", args = "intValue=260"))
    private int polydium$unlimitedLabel(int original) {
        return 1500;
    }
}
//?}
