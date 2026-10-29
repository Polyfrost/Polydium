package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
//? if >= 26.1 {
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
//?} else {
/*import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.BlockAndTintGetter;
*///?}
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.polyfrost.polydium.client.perf.EnclosedFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SectionCompiler.class)
public abstract class MixinEnclosedFluid {
    //? if >= 26.1 {
    @WrapWithCondition(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/FluidRenderer;tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V"))
    private boolean polydium$skipEnclosedFluid(FluidRenderer renderer, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState, @Share("fluidNeighbor") LocalRef<BlockPos.MutableBlockPos> neighborRef) {
    //?} else {
    /*@WrapWithCondition(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderLiquid(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V"))
    private boolean polydium$skipEnclosedFluid(BlockRenderDispatcher dispatcher, BlockPos pos, BlockAndTintGetter level, VertexConsumer consumer, BlockState blockState, FluidState fluidState, @Share("fluidNeighbor") LocalRef<BlockPos.MutableBlockPos> neighborRef) {
    *///?}
        BlockPos.MutableBlockPos neighbor = neighborRef.get();

        if (neighbor == null) {
            neighbor = new BlockPos.MutableBlockPos();
            neighborRef.set(neighbor);
        }

        return !EnclosedFluid.enclosed(level, pos, fluidState, neighbor);
    }
}
