package org.polyfrost.polydium.mixin.client.perf.sodiumextra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller", remap = false, priority = 1100)
public abstract class MixinExpandedCylindricalGate {
}
