package org.polyfrost.polydium.mixin.client.perf.scalablelux;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkAccess.class)
public interface ChunkSkyLightSourcesAccessor {
    @Accessor("skyLightSources")
    ChunkSkyLightSources polydium$getSkyLightSourcesRaw();
}
