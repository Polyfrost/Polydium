package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

public interface PersistentRingBuffer {
    @Nullable ByteBuffer polydium$persistentData(GpuBuffer buffer);
}
//?}
