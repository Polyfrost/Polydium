package org.polyfrost.polydium.client.perf.gpuprofile;

//? if >= 26.2 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
//? if >= 26.3 {
import com.mojang.renderpearl.api.commands.GpuQueryPool;
//?} else
/*import com.mojang.blaze3d.systems.GpuQueryPool;*/
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.mixin.client.perf.gpuprofile.VulkanQueryPoolAccessor;

final class PoolStamps implements GpuStamps {
    private static final long ALL_COMMANDS = 0x10000L;

    private final GpuQueryPool[] pools;
    private final long[] vkPools;
    private final double nanosPerTick;
    private final int validBits;
    private final CommandEncoder encoder;

    PoolStamps(int size) {
        GpuDevice device = RenderSystem.getDevice();

        pools   = new GpuQueryPool[GpuStamps.pools(size)];
        vkPools = new long[pools.length];

        try {
            for (int i = 0; i < pools.length; i++)
                pools[i] = device.createTimestampQueryPool(Math.min(POOL_SIZE, size - i * POOL_SIZE));

            if (pools[0] instanceof VulkanQueryPoolAccessor vulkan) {
                VulkanDevice backend = vulkan.polydium$device();

                validBits = GpuStamps.vulkanTimestampBits(backend.vkDevice(), backend.graphicsQueue().queueFamilyIndex());
            } else {
                validBits = 64;
            }
        } catch (Throwable error) {
            close();
            throw error;
        }

        nanosPerTick = device.getDeviceInfo().timestampPeriod();
        encoder      = device.createCommandEncoder();
    }

    @Override
    public void write(int query, @Nullable Object context) {
        int          index = query / POOL_SIZE;
        int          local = query % POOL_SIZE;
        GpuQueryPool pool  = pools[index];

        if (context instanceof RenderPass pass)
            pass.writeTimestamp(pool, local);
        else if (context instanceof CommandEncoder commands)
            commands.writeTimestamp(pool, local);
        else if (context instanceof VkCommandBuffer buffer)
            writeRaw(buffer, index, local);
        else
            encoder.writeTimestamp(pool, local);
    }

    private void writeRaw(VkCommandBuffer buffer, int index, int query) {
        long vkPool = vkPools[index];

        if (vkPool == 0L)
            vkPool = vkPools[index] = ((VulkanQueryPoolAccessor) pools[index]).polydium$vkQueryPool();

        VK12.vkResetQueryPool(buffer.getDevice(), vkPool, query, 1);
        KHRSynchronization2.vkCmdWriteTimestamp2KHR(buffer, ALL_COMMANDS, vkPool, query);
    }

    @Override
    public boolean read(int first, int count, long[] nanos) {
        return GpuStamps.readPools(first, count, nanos, validBits, nanosPerTick, (pool, query, n) -> pools[pool].getValues(query, n));
    }

    @Override
    public void close() {
        for (GpuQueryPool pool : pools) {
            if (pool != null)
                pool.close();
        }
    }
}
//?}
