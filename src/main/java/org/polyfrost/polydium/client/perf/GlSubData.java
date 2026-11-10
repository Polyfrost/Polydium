package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import org.polyfrost.polydium.mixin.client.perf.GlBufferAccessor;
import org.polyfrost.polydium.mixin.client.perf.GlBufferDirectAccessor;

import java.nio.ByteBuffer;

public final class GlSubData {
    private GlSubData() {
    }

    public static boolean applies(GpuBuffer buffer) {
        return buffer instanceof GlBuffer.Direct && !buffer.isClosed() && !((GlBufferAccessor) buffer).polydium$canPersistentMap();
    }

    public static void write(GpuBuffer buffer, long offset, ByteBuffer src) {
        GlBuffer gl = (GlBuffer) buffer;
        ((GlBufferDirectAccessor) gl).polydium$dsa().bufferSubData(gl.handle(), offset, src, gl.usage());
    }
}
//?}
