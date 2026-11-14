package org.polyfrost.polydium.client.perf.gpuprofile;

import org.jetbrains.annotations.Nullable;

import java.util.OptionalLong;
//? if >= 1.21.11 {
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;
//?}

public interface GpuStamps {
    int POOL_SIZE = 4096;
    int READ_CHUNK = 1024;

    static int pools(int size) {
        return (size + POOL_SIZE - 1) / POOL_SIZE;
    }

    static int chunk(int query, int remaining, int limit) {
        return Math.min(Math.min(limit, remaining), POOL_SIZE - query % POOL_SIZE);
    }

    static long ticksSince(long first, long ticks, int validBits) {
        int unused = 64 - validBits;

        return (ticks - first) << unused >> unused;
    }

    interface PoolReader {
        OptionalLong[] values(int pool, int query, int count);
    }

    static boolean readPools(int first, int count, long[] nanos, int validBits, double nanosPerTick, PoolReader reader) {
        long origin = 0L;

        for (int start = 0, chunk; start < count; start += chunk) {
            int            query  = first + start;
            OptionalLong[] values;

            chunk  = chunk(query, count - start, READ_CHUNK);
            values = reader.values(query / POOL_SIZE, query % POOL_SIZE, chunk);

            for (int i = 0; i < chunk; i++) {
                if (values[i].isEmpty())
                    return false;

                if (start + i == 0)
                    origin = values[i].getAsLong();

                nanos[start + i] = Math.round(ticksSince(origin, values[i].getAsLong(), validBits) * nanosPerTick);
            }
        }

        return true;
    }

    //? if >= 1.21.11 {
    static int vulkanTimestampBits(VkDevice device, int queueFamily) {
        int bits;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer count = stack.mallocInt(1);

            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device.getPhysicalDevice(), count, null);

            VkQueueFamilyProperties.Buffer families = VkQueueFamilyProperties.malloc(count.get(0), stack);

            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device.getPhysicalDevice(), count, families);
            bits = families.get(queueFamily).timestampValidBits();
        }

        if (bits == 0)
            throw new UnsupportedOperationException("the graphics queue cannot write timestamps (timestampValidBits is 0)");

        return bits;
    }
    //?}

    void write(int query, @Nullable Object context);

    default boolean accepts(@Nullable Object context) {
        return true;
    }

    boolean read(int first, int count, long[] nanos);

    default void recycle(int first, int count) {
    }

    void close();
}
