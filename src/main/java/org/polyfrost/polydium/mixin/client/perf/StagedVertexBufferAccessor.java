package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.StagedVertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StagedVertexBuffer.class)
public interface StagedVertexBufferAccessor {
    @Accessor("stagingBuffer")
    ByteBufferBuilder polydium$stagingBuffer();

    @Accessor("draws")
    List<?> polydium$draws();

    @Accessor("lastVertexBuilder")
    BufferBuilder polydium$lastVertexBuilder();

    @Accessor("lastVertexBuilder")
    void polydium$setLastVertexBuilder(BufferBuilder builder);

    @Accessor("lastBuildingDraw")
    void polydium$setLastBuildingDraw(StagedVertexBuffer.Draw draw);
}
//?}
