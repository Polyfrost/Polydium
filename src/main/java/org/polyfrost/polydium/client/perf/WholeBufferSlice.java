package org.polyfrost.polydium.client.perf;

//? if >= 26.3 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;

public interface WholeBufferSlice {
    GpuBufferSlice polydium$wholeSlice();

    void polydium$wholeSlice(GpuBufferSlice slice);
}
//?}
