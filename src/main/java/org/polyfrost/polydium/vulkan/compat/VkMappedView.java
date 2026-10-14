package org.polyfrost.polydium.vulkan.compat;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import java.nio.ByteBuffer;

public record VkMappedView(GpuBufferSlice slice, ByteBuffer data, Runnable onClose) implements GpuBuffer.MappedView {
  @Override
  public void close() {
    this.onClose.run();
  }
}
