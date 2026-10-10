package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 1.21.11 {
import com.mojang.renderpearl.api.buffers.GpuBuffer;
//? if >= 26.2 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice.MappedView;
//?} else {
/*import com.mojang.blaze3d.buffers.GpuBuffer.MappedView;
import com.mojang.blaze3d.systems.RenderSystem;
*///?}
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.polyfrost.polydium.client.perf.PersistentRingBuffer;
import org.polyfrost.polydium.mixin.client.perf.GlBufferAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(MappableRingBuffer.class)
public abstract class MixinMappableRingBufferPersistentMap implements PersistentRingBuffer {
    @Shadow
    @Final
    private GpuBuffer[] buffers;

    @Shadow
    private int current;

    @Unique
    private MappedView[] polydium$views;

    @Override
    public ByteBuffer polydium$persistentData(GpuBuffer buffer) {
        int index = this.current;
        if (this.buffers[index] != buffer) {
            return null;
        }
        MappedView[] views = this.polydium$views;
        if (views == null) {
            views = this.polydium$views = new MappedView[this.buffers.length];
        }
        MappedView view = views[index];
        if (view == null) {
            //? if >= 26.2 {
            if ((buffer.usage() & GpuBuffer.USAGE_MAP_WRITE) == 0 || buffer.size() > Integer.MAX_VALUE
                || buffer instanceof GlBuffer && !((GlBufferAccessor) buffer).polydium$canPersistentMap()) {
                return null;
            }
            view = views[index] = buffer.map(false, true);
            //?} else {
            /*if ((buffer.usage() & GpuBuffer.USAGE_MAP_WRITE) == 0 || buffer.size() > Integer.MAX_VALUE
                || buffer instanceof GlBuffer && ((GlBufferAccessor) buffer).polydium$persistentBuffer() == null) {
                return null;
            }
            view = views[index] = RenderSystem.getDevice().createCommandEncoder().mapBuffer(buffer, false, true);
            *///?}
        }
        return view.data();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void polydium$closePersistentViews(CallbackInfo ci) {
        MappedView[] views = this.polydium$views;
        if (views == null) {
            return;
        }
        this.polydium$views = null;
        for (MappedView view : views) {
            if (view != null) {
                view.close();
            }
        }
    }
}
//?}
