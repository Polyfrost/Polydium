package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SodiumGlIndirect {
    public static final int COMMAND_SIZE = 20;
    private static final long RING_SIZE = 4L << 20;
    private static final long PREPARED_GROUP_SIZE = 512L << 10;
    private static final int REPORT_AFTER = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger("polydium/perf");

    public static boolean batchDrawing;

    public static boolean irisTessellation;

    private static int supported = -1;
    private static int persistent = -1;
    private static GpuBuffer ring;
    private static long offset;
    private static final ReferenceArrayList<Batch> pending = new ReferenceArrayList<>();
    private static long generation;
    private static long scratch;
    private static long scratchSize;
    private static long preparedStart = -1;
    private static long preparedEnd;
    private static int prepares;
    private static long preparedDraws;
    private static long uploadedDraws;

    private SodiumGlIndirect() {
    }

    public interface Batch {
        long polydium$commands();

        int polydium$commandBytes();

        void polydium$prepared(GpuBufferSlice commands, long generation);
    }

    public static long generation() {
        return generation;
    }

    private static boolean persistent() {
        if (persistent < 0) {
            persistent = RenderSystem.getDevice().getDeviceInfo().features().persistentMapping() ? 1 : 0;
        }

        return persistent == 1;
    }

    public static boolean canPrepare() {
        return supported();
    }

    public static void beginPrepare() {
        generation++;
        pending.clear();
        preparedStart = -1;
        if (prepares < REPORT_AFTER && ++prepares == REPORT_AFTER) {
            LOGGER.info("Sodium GL indirect terrain: {} path, {} of {} draws prepared",
                    persistent() ? "device copy" : "buffer sub-data", preparedDraws, preparedDraws + uploadedDraws);
        }
    }

    public static void countDraw(boolean prepared) {
        if (prepares < REPORT_AFTER) {
            if (prepared) {
                preparedDraws++;
            } else {
                uploadedDraws++;
            }
        }
    }

    public static void queue(Batch batch) {
        if (batch.polydium$commands() != 0) {
            pending.add(batch);
        }
    }

    public static void flushPrepared() {
        int count = pending.size();
        if (count == 0) {
            return;
        }

        try {
            if (persistent()) {
                copyToDevice(count);
            } else {
                uploadToRing(count);
            }
        } finally {
            pending.clear();
        }
    }

    private static void uploadToRing(int count) {
        long total = 0;
        for (int i = 0; i < count; i++) {
            total += pending.get(i).polydium$commandBytes();
        }

        if (total > scratchSize) {
            scratch = MemoryUtil.nmemRealloc(scratch, total);
            scratchSize = total;
        }

        long written = 0;
        for (int i = 0; i < count; i++) {
            Batch batch = pending.get(i);
            int bytes = batch.polydium$commandBytes();
            MemoryUtil.memCopy(batch.polydium$commands(), scratch + written, bytes);
            written += bytes;
        }

        GpuBufferSlice target = ringUpload(scratch, Math.toIntExact(total));
        preparedStart = target.offset();
        preparedEnd = preparedStart + total;
        written = 0;
        for (int i = 0; i < count; i++) {
            Batch batch = pending.get(i);
            int bytes = batch.polydium$commandBytes();
            batch.polydium$prepared(target.slice(written, bytes), generation);
            written += bytes;
        }
    }

    private static void copyToDevice(int count) {
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        var memory = encoder.transientMemory();
        int start = 0;
        while (start < count) {
            long total = pending.get(start).polydium$commandBytes();
            int end = start + 1;
            while (end < count && total + pending.get(end).polydium$commandBytes() <= PREPARED_GROUP_SIZE) {
                total += pending.get(end).polydium$commandBytes();
                end++;
            }

            GpuBufferSlice target = memory.allocateGpu(total, 4, GpuBuffer.USAGE_INDIRECT_PARAMETERS | GpuBuffer.USAGE_COPY_DST);
            GpuBufferSlice source;
            try (GpuBufferSlice.MappedView staging = memory.allocateStaging(total, 4, GpuBuffer.USAGE_COPY_SRC)) {
                long dst = MemoryUtil.memAddress(staging.data());
                long written = 0;
                for (int i = start; i < end; i++) {
                    Batch batch = pending.get(i);
                    int bytes = batch.polydium$commandBytes();
                    MemoryUtil.memCopy(batch.polydium$commands(), dst + written, bytes);
                    batch.polydium$prepared(target.slice(written, bytes), generation);
                    written += bytes;
                }
                source = staging.slice();
            }

            encoder.copyToBuffer(source, target);
            start = end;
        }
    }

    public static boolean supported() {
        if (supported < 0) {
            supported = RenderSystem.getDevice().getDeviceInfo().features().multiDrawIndirect() ? 1 : 0;
        }

        return supported == 1;
    }

    public static GpuBufferSlice upload(long commands, int bytes) {
        if (persistent()) {
            return RenderSystem.getDevice().createCommandEncoder().transientMemory()
                    .uploadGpu(MemoryUtil.memByteBuffer(commands, bytes), 4, GpuBuffer.USAGE_INDIRECT_PARAMETERS);
        }

        return ringUpload(commands, bytes);
    }

    private static GpuBufferSlice ringUpload(long commands, int bytes) {
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

        if (preparedStart >= 0 && offset < preparedEnd && offset + bytes > preparedStart) {
            generation++;
            preparedStart = -1;
        }

        GlStateManager._glBindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, ((GlBuffer) ring).handle());
        GL15C.nglBufferSubData(GL40C.GL_DRAW_INDIRECT_BUFFER, offset, bytes, commands);
        GpuBufferSlice slice = ring.slice(offset, bytes);
        offset += bytes;
        return slice;
    }
}
//?}
