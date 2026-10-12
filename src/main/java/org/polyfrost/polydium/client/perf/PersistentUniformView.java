package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 && < 26.2 {
/*import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlBuffer;
import org.polyfrost.polydium.mixin.client.perf.DirectStateAccessInvoker;
import org.polyfrost.polydium.mixin.client.perf.GlBufferAccessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PersistentUniformView implements GpuBuffer.MappedView {
    private GlBuffer buffer;
    private long offset;
    private long length;
    private ByteBuffer data;

    public PersistentUniformView over(GlBuffer buffer, ByteBuffer persistent, GpuBufferSlice slice) {
        this.buffer = buffer;
        this.offset = slice.offset();
        this.length = slice.length();
        this.data = persistent.slice((int) this.offset, (int) this.length).order(ByteOrder.nativeOrder());
        return this;
    }

    @Override
    public ByteBuffer data() {
        return this.data;
    }

    @Override
    public void close() {
        GlBufferAccessor accessor = (GlBufferAccessor) this.buffer;
        ((DirectStateAccessInvoker) accessor.polydium$dsa()).polydium$flushMappedBufferRange(accessor.polydium$handle(), /^? if >= 1.21.11 {^/this.offset, this.length/^?} else {^//^(int) this.offset, (int) this.length^//^?}^/ /^? if >= 1.21.10 {^/, this.buffer.usage()/^?}^/);
    }
}
*///?}
