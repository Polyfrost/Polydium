package org.polyfrost.polydium.client.perf;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

public interface RegionOcclusion {

    long[] polydium$getHiddenSections();

    long[] polydium$getTestedSections();

    long[] polydium$getSectionBounds();

    long[] polydium$getChangedAt();

    int[] polydium$getAnimatedBounds();

    int polydium$getTestedGeneration();

    void polydium$setTestedGeneration(int generation);

    long polydium$getHiddenAfter();

    void polydium$setHiddenAfter(long captures);

    void polydium$clearBatches();

    boolean polydium$takeRefill(TerrainRenderPass pass);
}
