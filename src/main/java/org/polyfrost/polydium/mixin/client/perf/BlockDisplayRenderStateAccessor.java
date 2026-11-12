package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.BlockDisplayEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockDisplayEntityRenderState.class)
public interface BlockDisplayRenderStateAccessor {
    @Mutable
    @Accessor("blockModel")
    void polydium$setBlockModel(BlockModelRenderState blockModel);
}
//?}
