package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {
import com.mojang.renderpearl.api.buffers.GpuBuffer;
//? if >= 1.21.11
import org.jspecify.annotations.Nullable;
import net.minecraft.client.renderer.MappableRingBuffer;

import java.nio.ByteBuffer;

public interface PersistentRingBuffer {
    /*? if >= 1.21.11 {*/@Nullable /*?}*/ByteBuffer polydium$persistentData(GpuBuffer buffer);

    static /*? if >= 1.21.11 {*/@Nullable /*?}*/ByteBuffer persistentData(MappableRingBuffer ring, GpuBuffer buffer) {
        return ring instanceof PersistentRingBuffer persistent ? persistent.polydium$persistentData(buffer) : null;
    }
}
//?}
