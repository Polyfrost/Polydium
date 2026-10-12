package org.polyfrost.polydium.mixin.client.perf.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache", remap = false)
public abstract class MixinSectionCopyCacheCleanup {
    @Shadow @Final
    private static long MAX_CACHE_DURATION;
    @Shadow @Final
    private Long2ReferenceLinkedOpenHashMap<ClonedChunkSection> positionToEntry;
    @Shadow
    private long time;

    @Shadow
    private static long getMonotonicTimeSource() {
        throw new AssertionError();
    }

    @WrapMethod(method = "cleanup")
    private void polydium$cleanupFromFront(Operation<Void> original) {
        Long2ReferenceLinkedOpenHashMap<ClonedChunkSection> map = this.positionToEntry;
        long now = this.time = getMonotonicTimeSource();
        while (!map.isEmpty() && now > map.get(map.firstLongKey()).getLastUsedTimestamp() + MAX_CACHE_DURATION) {
            map.removeFirst();
        }
    }
}
