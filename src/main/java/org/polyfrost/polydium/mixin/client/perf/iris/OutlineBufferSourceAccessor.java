package org.polyfrost.polydium.mixin.client.perf.iris;

//? if >= 1.21.11 && < 26.2 {
/*import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OutlineBufferSource.class)
public interface OutlineBufferSourceAccessor {
    @Accessor("outlineBufferSource")
    MultiBufferSource.BufferSource polydium$bufferSource();
}
*///?} else {
public interface OutlineBufferSourceAccessor {
}
//?}
