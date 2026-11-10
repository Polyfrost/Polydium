package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.BlockStateCacheBatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 26.3
import net.minecraft.SharedConstants;

@Mixin(Blocks.class)
public abstract class MixinBlocksParallelCache {
    //? if >= 26.3 {
    @WrapOperation(method = "lambda$static$429", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;initCache()V"))
    //?} else
    //@WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;initCache()V"))
    private static void polydium$deferCache(BlockState state, Operation<Void> original) {
        //? if >= 26.3 {
        boolean debugSolid = SharedConstants.DEBUG_CALCULATE_SOLID;
        //?} else
        //boolean debugSolid = false;
        if (!PolydiumToggles.parallelBlockStateCache || !BlockStateCacheBatch.collecting || debugSolid) {
            original.call(state);
            return;
        }

        BlockStateCacheBatch.defer(state);
    }
}
