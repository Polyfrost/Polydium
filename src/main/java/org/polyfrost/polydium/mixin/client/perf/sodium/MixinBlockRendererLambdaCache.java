package org.polyfrost.polydium.mixin.client.perf.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer", priority = 1100, remap = false)
public abstract class MixinBlockRendererLambdaCache {
}
