package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.common.BaseGpuBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BaseGpuBuffer.class)
public abstract class MixinWholeBufferSlice implements GpuBuffer {
    @Unique
    private GpuBufferSlice polydium$wholeSlice;

    @Override
    public GpuBufferSlice slice() {
        GpuBufferSlice slice = this.polydium$wholeSlice;
        if (slice == null) {
            this.polydium$wholeSlice = slice = new GpuBufferSlice(this, 0L, this.size());
        }
        return slice;
    }
}
//?}
