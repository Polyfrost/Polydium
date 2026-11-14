package org.polyfrost.polydium.mixin.client.perf.iris;

//? if >= 1.21.11 && < 26.2 {
/*import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MultiBufferSource.BufferSource.class)
public interface BufferSourceAccessor {
    @Accessor("startedBuilders")
    Map<RenderType, BufferBuilder> polydium$startedBuilders();

    @Accessor("lastSharedType")
    void polydium$setLastSharedType(RenderType type);
}
*///?} else {
public interface BufferSourceAccessor {
}
//?}
