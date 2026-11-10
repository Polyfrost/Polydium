package org.polyfrost.polydium.mixin.client.perf.lambdynlights;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "dev.lambdaurora.lambdynlights.LambDynLights", remap = false)
public abstract class MixinLightSourceSetRemove {
}
