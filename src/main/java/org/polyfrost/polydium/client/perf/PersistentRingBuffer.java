package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {
import com.mojang.renderpearl.api.buffers.GpuBuffer;
//? if >= 1.21.11
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

public interface PersistentRingBuffer {
    /*? if >= 1.21.11 {*/@Nullable /*?}*/ByteBuffer polydium$persistentData(GpuBuffer buffer);
}
//?}
