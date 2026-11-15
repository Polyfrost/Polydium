package org.polyfrost.polydium.client.perf;

/**
 * Entity Culling's filtered block entity list for one chunk, valid while the chunk's block entity map and the
 * generation (renderer reloads, whitelist edits) are unchanged.
 */
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
