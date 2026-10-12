package org.polyfrost.polydium.client.perf;

public interface RegionOcclusion {

    long[] polydium$getHiddenSections();

    long[] polydium$getTestedSections();

    int polydium$getTestedGeneration();

    void polydium$setTestedGeneration(int generation);
}
