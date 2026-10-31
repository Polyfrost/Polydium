package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import net.minecraft.client.gui.components.debug.DebugEntryFps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DebugEntryFps.class)
public abstract class MixinFpsLimitDebugEntry {
    @ModifyConstant(method = "display", constant = @Constant(intValue = 260))
    private int polydium$unlimitedLabel(int original) {
        return 1500;
    }
}
//?}
