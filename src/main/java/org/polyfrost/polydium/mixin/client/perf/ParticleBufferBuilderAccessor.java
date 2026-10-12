package org.polyfrost.polydium.mixin.client.perf;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface ParticleBufferBuilderAccessor {
    @Accessor("format")
    VertexFormat polydium$format();

    @Accessor("buffer")
    ByteBufferBuilder polydium$buffer();

    @Accessor("vertexSize")
    int polydium$vertexSize();

    @Accessor("vertices")
    int polydium$vertices();

    @Accessor("vertices")
    void polydium$setVertices(int vertices);

    @Accessor("vertexPointer")
    void polydium$setVertexPointer(long pointer);

    @Accessor("elementsToFill")
    void polydium$setElementsToFill(int elements);
}
