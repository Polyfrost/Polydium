package org.polyfrost.polydium.mixin.client.perf.gammautils;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = OptionInstance.class, priority = 1100)
public abstract class MixinGammaOptionGate {
}
