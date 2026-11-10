package org.polyfrost.polydium.vulkan.compat;

import org.polyfrost.polydium.vulkan.compat.GpuFormat;

public record DeviceLimits(
  int maxAnisotropy,
  int minUniformOffsetAlignment,
  int maxTextureSize,
  long maxMemoryAllocationSize,
  int maxMultiDrawDirectInterleavedDrawCount,
  int maxColorAttachments
) {
  public int maxTextureSizeForFormat(final GpuFormat format) {
    return Integer.highestOneBit(Math.min(this.maxTextureSize, (int)Math.sqrt((double)this.maxMemoryAllocationSize / format.blockSize())));
  }
}
