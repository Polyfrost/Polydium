package org.polyfrost.polydium.vulkan.sodium;

import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.gl.arena.staging.StagingBuffer;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;

import java.nio.ByteBuffer;

public final class VkStagingBuffer implements StagingBuffer {
    @Override
    public void enqueueCopy(CommandList commandList, ByteBuffer data, GlBuffer dst, long writeOffset) {
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(VkCommandList.gpu(dst).slice(writeOffset, data.remaining()), data);
    }

    @Override
    public void flush(CommandList commandList) {
    }

    @Override
    public void delete(CommandList commandList) {
    }

    @Override
    public void flip() {
    }

    @Override
    public long getUploadSizeLimit(long frameDuration) {
        return 25600000;
    }

    @Override
    public String toString() {
        return "Vulkan";
    }
}
