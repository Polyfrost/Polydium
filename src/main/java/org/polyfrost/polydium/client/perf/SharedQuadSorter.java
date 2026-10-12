package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
import com.mojang.blaze3d.vertex.CompactVectorArray;
//?} else {
/*import org.joml.Vector3f;
*///?}

public interface SharedQuadSorter {
    int[] polydium$sortShared(/*? if >= 1.21.10 {*/CompactVectorArray/*?} else {*//*Vector3f[]*//*?}*/ centroids);
}
