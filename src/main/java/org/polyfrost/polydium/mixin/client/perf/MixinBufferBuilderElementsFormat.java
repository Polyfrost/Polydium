package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import org.polyfrost.polydium.client.perf.BufferBuilderElements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VertexFormat.class)
public class MixinBufferBuilderElementsFormat implements BufferBuilderElements {
    @Unique
    private volatile VertexFormatElement[] polydium$bufferElements;

    @Override
    public VertexFormatElement[] polydium$getBufferElements() {
        return this.polydium$bufferElements;
    }

    @Override
    public void polydium$setBufferElements(VertexFormatElement[] elements) {
        this.polydium$bufferElements = elements;
    }
}
//?}
