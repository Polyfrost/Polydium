package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
//? if < 26.3 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
*///?}
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
//? if >= 26.3
import com.mojang.renderpearl.backend.common.BaseGpuBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

//? if >= 26.3 {
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
//?} else if < 26.2 {
/*@Mixin(GpuBuffer.class)
public abstract class MixinWholeBufferSlice {
    @Unique
    private GpuBufferSlice polydium$wholeSlice;

    @WrapMethod(method = "slice()Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;")
    private GpuBufferSlice polydium$cachedWholeSlice(Operation<GpuBufferSlice> original) {
        GpuBufferSlice slice = this.polydium$wholeSlice;
        if (slice == null) {
            this.polydium$wholeSlice = slice = original.call();
        }
        return slice;
    }
}
*///?}
//?}
