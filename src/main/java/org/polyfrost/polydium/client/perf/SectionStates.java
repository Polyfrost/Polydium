package org.polyfrost.polydium.client.perf;

import net.caffeinemc.mods.sodium.client.world.PalettedContainerROExtension;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainerRO;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class SectionStates {
    private static final int LIMIT = Math.max(0, Integer.getInteger("polydium.sharedStates", 1024));
    public static final int CACHE_SIZE = Math.max(64, Integer.getInteger("polydium.sectionCache", 2048));

    private static final AtomicReferenceArray<Shared> KEPT = new AtomicReferenceArray<>(Math.max(1, LIMIT));
    private static final AtomicInteger NEXT = new AtomicInteger();
    private static final ConcurrentLinkedQueue<BlockState[]> POOL = new ConcurrentLinkedQueue<>();

    public interface Access {
        Shared polydium$states();

        void polydium$states(Shared states);
    }

    public static final class Shared extends AtomicInteger {
        final BlockState[] states;
        private volatile boolean retired;

        Shared(BlockState[] states) {
            super(1);
            this.states = states;
        }

        boolean acquire() {
            for (int refs; (refs = this.get()) >= 0; ) {
                if (this.compareAndSet(refs, refs + 1)) {
                    if (!this.retired) {
                        return true;
                    }
                    this.release();
                    return false;
                }
            }
            return false;
        }

        public void release() {
            if (this.decrementAndGet() == 0 && this.retired) {
                this.reclaim();
            }
        }

        void retire() {
            this.retired = true;
            this.reclaim();
        }

        private void reclaim() {
            if (this.compareAndSet(0, -1)) {
                POOL.offer(this.states);
            }
        }
    }

    private static final class Air {
        static final BlockState[] STATES = new BlockState[4096];

        static {
            Arrays.fill(STATES, Blocks.AIR.defaultBlockState());
        }
    }

    private SectionStates() {
    }

    public static BlockState[] shared(ClonedChunkSection section, ChunkRenderContext context, Shared[] held, int index) {
        PalettedContainerRO<BlockState> data = section.getBlockData();
        if (data == null) {
            return Air.STATES;
        }
        if (LIMIT == 0 || !(section instanceof Access access)) {
            return null;
        }

        Shared shared = access.polydium$states();
        if (shared != null && shared.acquire()) {
            held[index] = shared;
            return shared.states;
        }
        if (!section.getPosition().equals(context.getOrigin())) {
            return null;
        }

        BlockState[] states = POOL.poll();
        if (states == null) {
            states = new BlockState[4096];
        }
        PalettedContainerROExtension.of(data).sodium$unpack(states);
        shared = new Shared(states);
        access.polydium$states(shared);
        Shared evicted = KEPT.getAndSet(Math.floorMod(NEXT.getAndIncrement(), KEPT.length()), shared);
        if (evicted != null) {
            evicted.retire();
        }
        held[index] = shared;
        return states;
    }

    public static void release(Shared[] held) {
        for (int i = 0; i < held.length; i++) {
            Shared shared = held[i];
            if (shared != null) {
                held[i] = null;
                shared.release();
            }
        }
    }
}
