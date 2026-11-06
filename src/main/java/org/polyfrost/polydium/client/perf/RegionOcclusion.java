package org.polyfrost.polydium.client.perf;

public interface RegionOcclusion {

    long[] polydium$getHiddenSections();

    long[] polydium$getTestedSections();

    long[] polydium$getSectionBounds();

    int[] polydium$getAnimatedBounds();

    int polydium$getTestedGeneration();

    void polydium$setTestedGeneration(int generation);

    long polydium$getHiddenAfter();

    void polydium$setHiddenAfter(long captures);
}
