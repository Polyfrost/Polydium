package org.polyfrost.polydium.mixin.client.perf.scalablelux;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LevelChunk.class, ProtoChunk.class})
public abstract class MixinSkyLightSourcesUpdate {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void polydium$updateSkyLightSources(CallbackInfoReturnable<BlockState> cir, @Local(argsOnly = true) BlockPos pos) {
        if (cir.getReturnValue() == null)
            return;
        ChunkSkyLightSources sources = ((ChunkSkyLightSourcesAccessor) this).polydium$getSkyLightSourcesRaw();
        if (sources != null)
            sources.update((ChunkAccess) (Object) this, pos.getX() & 15, pos.getY(), pos.getZ() & 15);
    }
}
