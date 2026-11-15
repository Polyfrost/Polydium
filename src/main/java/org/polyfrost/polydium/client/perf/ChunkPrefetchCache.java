package org.polyfrost.polydium.client.perf;

public interface ChunkPrefetchCache {
    Object[] polydium$storedPrefetch();

    int polydium$prefetchGeneration();

    void polydium$setPrefetch(Object[] cullables, int generation);

    default Object[] polydium$prefetch(int generation) {
        return generation == this.polydium$prefetchGeneration() ? this.polydium$storedPrefetch() : null;
    }

    default void polydium$invalidatePrefetch() {
        this.polydium$setPrefetch(null, 0);
    }
}
