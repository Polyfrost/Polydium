package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 && < 26.2 {
/*import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.MappableRingBuffer;

import java.nio.ByteBuffer;

public final class ImmediateDrawRing {
    private static final int USAGE = GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_INDEX;
    private static final int MAX_SIZE = 1 << 30;

    private static MappableRingBuffer ring;
    private static GpuBuffer active;
    private static int size = 4 << 20;
    private static int cursor;
    private static long demand;
    private static int vertexOffset;

    private ImmediateDrawRing() {
    }

    public static void setVertexOffset(int offset) {
        vertexOffset = offset;
    }

    public static long vertexOffset() {
        return vertexOffset;
    }

    public static boolean holds(GpuBuffer buffer) {
        return buffer != null && buffer == active;
    }

    public static int upload(ByteBuffer src, int align) {
        int length = src.remaining();
        demand += length + align;
        int offset = (cursor + align - 1) / align * align;
        if ((long) offset + length > size) {
            return -1;
        }
        if (ring == null) {
            ring = new MappableRingBuffer(() -> "Polydium immediate draw ring", USAGE, size);
        }
        active = ring.currentBuffer();
        ByteBuffer data = PersistentRingBuffer.persistentData(ring, active);
        if (data != null) {
            data.put(offset, src, src.position(), length);
        } else {
            try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(active.slice(offset, length), false, true)) {
                view.data().put(0, src, src.position(), length);
            }
        }
        cursor = offset + length;
        return offset;
    }

    public static GpuBuffer buffer() {
        return active;
    }

    public static void endFrame() {
        active = null;
        if (cursor > 0) {
            ring.rotate();
            cursor = 0;
        }
        if (demand > size && size < MAX_SIZE) {
            size = (int) Math.min(MAX_SIZE, Long.highestOneBit(demand - 1) << 1);
            close();
        }
        demand = 0;
    }

    public static void close() {
        if (ring != null) {
            ring.close();
            ring = null;
        }
        active = null;
        cursor = 0;
    }
}
*///?}
