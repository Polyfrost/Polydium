package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrameRenderState.class)
public interface ItemFrameRenderStateAccessor {
    @Mutable
    @Accessor("frameModel")
    void polydium$setFrameModel(BlockModelRenderState frameModel);
}
//?}
