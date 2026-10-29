package org.polyfrost.polydium.mixin.client.perf.sodium;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.polyfrost.polydium.client.perf.EnclosedFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask", remap = false)
public abstract class MixinSodiumEnclosedFluid {
    @WrapWithCondition(method = "execute", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;render(Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/TranslucentGeometryCollector;Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V"))
    private boolean polydium$skipEnclosedFluid(FluidRenderer renderer, LevelSlice level, BlockState blockState, FluidState fluidState, BlockPos pos, BlockPos offset, TranslucentGeometryCollector collector, ChunkBuildBuffers buffers, @Share("fluidNeighbor") LocalRef<BlockPos.MutableBlockPos> neighborRef) {
        BlockPos.MutableBlockPos neighbor = neighborRef.get();

        if (neighbor == null) {
            neighbor = new BlockPos.MutableBlockPos();
            neighborRef.set(neighbor);
        }

        return !EnclosedFluid.enclosed(level, pos, fluidState, neighbor);
    }
}
