package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.ChunkPrefetchCache

class ChunkPrefetchCacheTest {
    private class Chunk : ChunkPrefetchCache {
        var list: Array<Any>? = null
        var generation = 0
        override fun `polydium$storedPrefetch`() = list
        override fun `polydium$prefetchGeneration`() = generation
        override fun `polydium$setPrefetch`(cullables: Array<Any>?, generation: Int) {
            list = cullables
            this.generation = generation
        }
    }

    @Test
    fun `a list only survives while its chunk and generation are unchanged`() {
        val chunk = Chunk()
        val list = arrayOf<Any>("chest")
        assertNull(chunk.`polydium$prefetch`(1))
        chunk.`polydium$setPrefetch`(list, 1)
        assertSame(list, chunk.`polydium$prefetch`(1))
        assertNull(chunk.`polydium$prefetch`(2))
        chunk.`polydium$invalidatePrefetch`()
        assertNull(chunk.`polydium$prefetch`(1))
    }
}
