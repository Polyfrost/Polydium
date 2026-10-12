package org.polyfrost.polydium.client.perf;

import net.caffeinemc.mods.sodium.client.world.PalettedContainerROExtension;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainerRO;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class SectionStates {
    private static final int LIMIT = Math.max(0, Integer.getInteger("polydium.sharedStates", 1024));
    public static final int CACHE_SIZE = Math.max(64, Integer.getInteger("polydium.sectionCache", 2048));

    private static final AtomicReferenceArray<WeakReference<Access>> KEPT = new AtomicReferenceArray<>(Math.max(1, LIMIT));
    private static final AtomicInteger NEXT = new AtomicInteger();

    public interface Access {
        BlockState[] polydium$states();

        void polydium$states(BlockState[] states);
    }

    private static final class Air {
        static final BlockState[] STATES = new BlockState[4096];

        static {
            Arrays.fill(STATES, Blocks.AIR.defaultBlockState());
        }
    }

    private SectionStates() {
    }

    public static BlockState[] shared(ClonedChunkSection section, ChunkRenderContext context) {
        PalettedContainerRO<BlockState> data = section.getBlockData();
        if (data == null) {
            return Air.STATES;
        }
        if (LIMIT == 0 || !(section instanceof Access access)) {
            return null;
        }

        BlockState[] states = access.polydium$states();
        if (states == null && section.getPosition().equals(context.getOrigin())) {
            states = new BlockState[4096];
            PalettedContainerROExtension.of(data).sodium$unpack(states);
            keep(access, states, KEPT, NEXT);
        }
        return states;
    }

    public static void keep(Access section, BlockState[] states, AtomicReferenceArray<WeakReference<Access>> kept, AtomicInteger next) {
        section.polydium$states(states);
        WeakReference<Access> evicted = kept.getAndSet(Math.floorMod(next.getAndIncrement(), kept.length()), new WeakReference<>(section));
        Access old = evicted == null ? null : evicted.get();
        if (old != null && old != section) {
            old.polydium$states(null);
        }
    }
}
