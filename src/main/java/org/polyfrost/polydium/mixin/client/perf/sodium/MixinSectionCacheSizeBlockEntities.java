package org.polyfrost.polydium.mixin.client.perf.sodium;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection", remap = false)
public abstract class MixinSectionCacheSizeBlockEntities {
    @WrapMethod(method = "tryCopyBlockEntities", require = 0)
    private static Int2ReferenceMap<BlockEntity> polydium$skipEmptyChunk(LevelChunk chunk, SectionPos pos, Operation<Int2ReferenceMap<BlockEntity>> original) {
        return PolydiumToggles.sectionCacheSize && chunk.getBlockEntities().isEmpty() ? null : original.call(chunk, pos);
    }
}
//?}
