package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {

public interface RegionOcclusion {

    long[] polydium$getHiddenSections();

    long[] polydium$getTestedSections();

    int polydium$getTestedGeneration();

    void polydium$setTestedGeneration(int generation);
}
//?}
