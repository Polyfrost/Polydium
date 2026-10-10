package org.polyfrost.polydium.client.perf;

//? if >= 26.1 {
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.RayOcclusionSectionTree;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.SectionTree;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.BaseBiForest;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.BaseMultiForest;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.Forest;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.Tree;
import org.polyfrost.polydium.mixin.client.perf.sodium.MixinTreeArrayAccessor;
import org.polyfrost.polydium.mixin.client.perf.sodium.MixinBiForestAccessor;
import org.polyfrost.polydium.mixin.client.perf.sodium.MixinMultiForestAccessor;
import org.polyfrost.polydium.mixin.client.perf.sodium.MixinRayTreeAccessor;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class TreeArrayPool {
    public static final int TREE_LONGS = 4096;
    private static final int MAX_POOLED = 64;
    private static final ConcurrentLinkedQueue<long[]> POOL = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger POOLED = new AtomicInteger();

    private TreeArrayPool() {
    }

    public static long[] acquire() {
        long[] pooled = POOL.poll();
        if (pooled == null) {
            return new long[TREE_LONGS];
        }
        POOLED.decrementAndGet();
        Arrays.fill(pooled, 0L);
        return pooled;
    }

    public static void release(SectionTree sectionTree) {
        releaseForest(sectionTree.tree);
        if (sectionTree instanceof RayOcclusionSectionTree rayTree) {
            releaseForest(((MixinRayTreeAccessor) rayTree).polydium$portalTree());
        }
    }

    private static void releaseForest(Forest<?> forest) {
        if (forest instanceof BaseBiForest<?> biForest) {
            MixinBiForestAccessor accessor = (MixinBiForestAccessor) biForest;
            releaseTree(accessor.polydium$mainTree());
            releaseTree(accessor.polydium$secondaryTree());
        } else if (forest instanceof BaseMultiForest<?> multiForest) {
            for (Tree tree : ((MixinMultiForestAccessor) multiForest).polydium$trees()) {
                releaseTree(tree);
            }
        }
    }

    private static void releaseTree(Tree tree) {
        if (tree == null) {
            return;
        }
        long[] array = ((MixinTreeArrayAccessor) tree).polydium$tree();
        if (array.length != TREE_LONGS) {
            return;
        }
        if (POOLED.incrementAndGet() > MAX_POOLED) {
            POOLED.decrementAndGet();
            return;
        }
        POOL.add(array);
    }
}
//?}
