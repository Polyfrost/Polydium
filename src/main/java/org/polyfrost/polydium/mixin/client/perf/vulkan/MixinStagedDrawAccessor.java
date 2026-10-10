package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.renderpearl.api.pipeline.IndexType;
import net.minecraft.client.renderer.StagedVertexBuffer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(StagedVertexBuffer.Draw.class)
public interface MixinStagedDrawAccessor {
    @Accessor("vertexBufferSlices")
    List<ByteBufferBuilder.Result> polydium$vertexBufferSlices();

    @Accessor("vertexOffset")
    int polydium$vertexOffset();

    @Accessor("indexOffset")
    int polydium$indexOffset();

    @Accessor("quadSorting")
    @Nullable VertexSorting polydium$quadSorting();

    @Invoker("indexType")
    IndexType polydium$indexType();

    @Invoker("freeVertexData")
    void polydium$freeVertexData();
}
//?}
