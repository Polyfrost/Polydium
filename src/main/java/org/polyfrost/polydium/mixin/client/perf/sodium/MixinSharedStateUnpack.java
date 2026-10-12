package org.polyfrost.polydium.mixin.client.perf.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection;
import net.minecraft.world.level.block.state.BlockState;
import org.polyfrost.polydium.client.perf.SectionStates;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.LevelSlice", remap = false)
public abstract class MixinSharedStateUnpack {
    @Shadow @Final
    private BlockState[][] blockArrays;

    @Unique
    private BlockState[][] polydium$ownArrays;

    @WrapOperation(method = "copySectionData", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;unpackBlockData"), require = 0)
    private void polydium$shareUnpacked(LevelSlice slice, BlockState[] blockArray, ChunkRenderContext context, ClonedChunkSection section, Operation<Void> original, @Local(argsOnly = true) int sectionIndex) {
        if (this.polydium$ownArrays == null) {
            this.polydium$ownArrays = this.blockArrays.clone();
        }

        BlockState[] shared = SectionStates.shared(section, context);
        if (shared != null) {
            this.blockArrays[sectionIndex] = shared;
            return;
        }

        BlockState[] own = this.polydium$ownArrays[sectionIndex];
        this.blockArrays[sectionIndex] = own;
        original.call(slice, own, context, section);
    }
}
