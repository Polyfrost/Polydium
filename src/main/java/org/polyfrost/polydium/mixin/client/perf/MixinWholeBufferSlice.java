package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
//? if >= 26.3
import org.polyfrost.polydium.client.perf.WholeBufferSlice;
import org.spongepowered.asm.mixin.Mixin;
//? if < 26.3
/*import org.spongepowered.asm.mixin.Unique;*/
import org.spongepowered.asm.mixin.injection.At;

//? if >= 26.3 {
@Mixin(GpuBuffer.class)
public interface MixinWholeBufferSlice {
    @WrapOperation(method = "slice()Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;", at = @At(value = "NEW", target = "com/mojang/renderpearl/api/buffers/GpuBufferSlice"))
    private GpuBufferSlice polydium$cachedWholeSlice(GpuBuffer buffer, long offset, long length, Operation<GpuBufferSlice> original) {
        if (!(this instanceof WholeBufferSlice cache)) {
            return original.call(buffer, offset, length);
        }
        GpuBufferSlice slice = cache.polydium$wholeSlice();
        if (slice == null) {
            cache.polydium$wholeSlice(slice = original.call(buffer, offset, length));
        }
        return slice;
    }
}
//?} else if < 26.2 {
/*@Mixin(GpuBuffer.class)
public abstract class MixinWholeBufferSlice {
    @Unique
    private GpuBufferSlice polydium$wholeSlice;

    @WrapOperation(method = "slice()Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;", at = @At(value = "NEW", target = "com/mojang/renderpearl/api/buffers/GpuBufferSlice"))
    private GpuBufferSlice polydium$cachedWholeSlice(GpuBuffer buffer, /^? if >= 1.21.11 {^/long/^?} else {^//^int^//^?}^/ offset, /^? if >= 1.21.11 {^/long/^?} else {^//^int^//^?}^/ length, Operation<GpuBufferSlice> original) {
        GpuBufferSlice slice = this.polydium$wholeSlice;
        if (slice == null) {
            this.polydium$wholeSlice = slice = original.call(buffer, offset, length);
        }
        return slice;
    }
}
*///?}
//?}
