package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.system.MemoryUtil;

public final class SodiumGlIndirect {
    public static final int COMMAND_SIZE = 20;
    private static final long RING_SIZE = 4L << 20;

    public static boolean batchDrawing;

    public static boolean irisTessellation;

    private static int supported = -1;
    private static int persistent = -1;
    private static GpuBuffer ring;
    private static long offset;

    private SodiumGlIndirect() {
    }

    public static boolean supported() {
        if (supported < 0) {
            supported = RenderSystem.getDevice().getDeviceInfo().features().multiDrawIndirect() ? 1 : 0;
        }

        return supported == 1;
    }

    public static GpuBufferSlice upload(long commands, int bytes) {
        if (persistent < 0) {
            persistent = RenderSystem.getDevice().getDeviceInfo().features().persistentMapping() ? 1 : 0;
        }

        if (persistent == 1) {
            return RenderSystem.getDevice().createCommandEncoder().transientMemory()
                    .uploadGpu(MemoryUtil.memByteBuffer(commands, bytes), 4, GpuBuffer.USAGE_INDIRECT_PARAMETERS);
        }

        if (ring == null || ring.size() < bytes) {
            if (ring != null) {
                ring.close();
            }

            ring = RenderSystem.getDevice().createBuffer(() -> "Polydium Sodium indirect commands",
                    GpuBuffer.USAGE_INDIRECT_PARAMETERS | GpuBuffer.USAGE_COPY_DST, Math.max(RING_SIZE, bytes));
            offset = 0;
        }

        if (offset + bytes > ring.size()) {
            offset = 0;
        }

        GlStateManager._glBindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, ((GlBuffer) ring).handle());
        GL15C.nglBufferSubData(GL40C.GL_DRAW_INDIRECT_BUFFER, offset, bytes, commands);
        GpuBufferSlice slice = ring.slice(offset, bytes);
        offset += bytes;
        return slice;
    }
}
//?}
