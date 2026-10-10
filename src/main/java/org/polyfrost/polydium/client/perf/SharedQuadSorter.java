package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.mojang.blaze3d.vertex.CompactVectorArray;

public interface SharedQuadSorter {
    int[] polydium$sortShared(CompactVectorArray centroids);
}
//?}
