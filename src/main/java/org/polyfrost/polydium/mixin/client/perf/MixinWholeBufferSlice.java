package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
//? if >= 26.3
import org.polyfrost.polydium.client.perf.WholeBufferSlice;
import org.spongepowered.asm.mixin.Mixin;
//? if < 26.3
/*import org.spongepowered.asm.mixin.Unique;*/

//? if >= 26.3 {
@Mixin(GpuBuffer.class)
public interface MixinWholeBufferSlice {
    @WrapMethod(method = "slice()Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;")
    private GpuBufferSlice polydium$cachedWholeSlice(Operation<GpuBufferSlice> original) {
        if (!(this instanceof WholeBufferSlice cache)) {
            return original.call();
        }
        GpuBufferSlice slice = cache.polydium$wholeSlice();
        if (slice == null) {
            cache.polydium$wholeSlice(slice = original.call());
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
