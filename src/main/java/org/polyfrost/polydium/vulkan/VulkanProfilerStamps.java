package org.polyfrost.polydium.vulkan;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanSync;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuStamps;

public final class VulkanProfilerStamps implements GpuStamps {
    private static final long ALL_COMMANDS = 0x10000L;

    private final VulkanDevice device;
    private final VulkanQueryPool[] pools;
    private final double nanosPerTick;
    private final int validBits;

    public VulkanProfilerStamps(int size) {
        device = VulkanDevice.active;
        validBits = GpuStamps.vulkanTimestampBits(device.vkDevice(), device.graphicsQueue().queueFamilyIndex());
        pools = new VulkanQueryPool[GpuStamps.pools(size)];

        try {
            for (int i = 0; i < pools.length; i++)
                pools[i] = (VulkanQueryPool) device.createTimestampQueryPool(Math.min(POOL_SIZE, size - i * POOL_SIZE));
        } catch (Throwable error) {
            close();
            throw error;
        }

        nanosPerTick = device.getDeviceInfo().timestampPeriod();
    }

    @Override
    public void write(int query, @Nullable Object context) {
        VulkanSync.writeTimestamp((VkCommandBuffer) context, ALL_COMMANDS, pools[query / POOL_SIZE].vkQueryPool(), query % POOL_SIZE);
    }

    @Override
    public boolean accepts(@Nullable Object context) {
        return context instanceof VkCommandBuffer;
    }

    @Override
    public boolean read(int first, int count, long[] nanos) {
        return GpuStamps.readPools(first, count, nanos, validBits, nanosPerTick, (pool, query, n) -> pools[pool].getValues(query, n));
    }

    @Override
    public void recycle(int first, int count) {
        for (int start = 0, chunk; start < count; start += chunk) {
            int query = first + start;

            chunk = GpuStamps.chunk(query, count - start, POOL_SIZE);
            VK12.vkResetQueryPool(device.vkDevice(), pools[query / POOL_SIZE].vkQueryPool(), query % POOL_SIZE, chunk);
        }
    }

    @Override
    public void close() {
        for (VulkanQueryPool pool : pools) {
            if (pool != null)
                pool.close();
        }
    }
}
