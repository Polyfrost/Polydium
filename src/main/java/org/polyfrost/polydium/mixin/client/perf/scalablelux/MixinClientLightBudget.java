package org.polyfrost.polydium.mixin.client.perf.scalablelux;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "ca.spottedleaf.starlight.common.light.StarLightInterface", priority = 1100, remap = false)
public abstract class MixinClientLightBudget {
}
