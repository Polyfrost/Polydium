package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.mojang.renderpearl.api.vertex.VertexFormatElement;

public interface BufferBuilderElements {
    VertexFormatElement[] polydium$getBufferElements();

    void polydium$setBufferElements(VertexFormatElement[] elements);
}
//?}
