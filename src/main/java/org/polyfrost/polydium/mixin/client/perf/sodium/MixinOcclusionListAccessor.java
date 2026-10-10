package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList", remap = false)
public interface MixinOcclusionListAccessor {
    @Accessor(value = "sectionsWithGeometryMap", remap = false)
    long[] polydium$sectionsWithGeometryMap();
}
//?}
