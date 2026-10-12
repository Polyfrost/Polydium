package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.common.BaseGpuBuffer;
import org.polyfrost.polydium.client.perf.WholeBufferSlice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BaseGpuBuffer.class)
public abstract class MixinWholeBufferSliceHolder implements WholeBufferSlice {
    @Unique
    private GpuBufferSlice polydium$wholeSlice;

    @Override
    public GpuBufferSlice polydium$wholeSlice() {
        return this.polydium$wholeSlice;
    }

    @Override
    public void polydium$wholeSlice(GpuBufferSlice slice) {
        this.polydium$wholeSlice = slice;
    }
}
//?}
