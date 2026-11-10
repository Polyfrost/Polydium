package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 || < 1.21.4 {
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.quad.RegularTQuad", remap = false)
public interface RegularTQuadAccessor {
    @Accessor(value = "vertexPositions", remap = false)
    float[] polydium$vertexPositions();

    @Accessor(value = "vertexPositions", remap = false)
    void polydium$setVertexPositions(float[] positions);
}
//?}
