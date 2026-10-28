package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {
import com.mojang.renderpearl.api.buffers.GpuBuffer;
//? if >= 26.2 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
//?} else
/*import com.mojang.blaze3d.systems.RenderSystem;*/
import net.minecraft.client.renderer.MappableRingBuffer;

import java.nio.ByteBuffer;

public final class UniformRing {
    private UniformRing() {
    }

    public static GpuBuffer write(MappableRingBuffer ring, ByteBuffer src) {
        ring.rotate();
        GpuBuffer target = ring.currentBuffer();
        //? if >= 26.2 {
        if (target instanceof VulkanGpuBuffer.Direct || target instanceof GlBuffer.Direct) {
            ByteBuffer data = ((PersistentRingBuffer) ring).polydium$persistentData(target);
            if (data != null) {
                data.put(0, src, src.position(), src.remaining());
                return target;
            }
        }
        try (GpuBufferSlice.MappedView view = target.map(false, true)) {
            view.data().put(0, src, src.position(), src.remaining());
        }
        //?} else {
        /*try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(target, false, true)) {
            view.data().put(0, src, src.position(), src.remaining());
        }
        *///?}
        return target;
    }
}
//?}
