package org.polyfrost.polydium.vulkan.sodium;

import com.mojang.blaze3d.buffers.GpuBuffer;

public interface VkBacked {
    GpuBuffer polydium$gpu();

    void polydium$gpu(GpuBuffer buffer);
}
