package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.joml.Vector4f;

public final class UniversalCraftVulkan {
  private static final int GL_ALREADY_SIGNALED = 0x911A;
  private static final int GL_TIMEOUT_EXPIRED = 0x911B;

  private static final Long2ObjectOpenHashMap<GpuFence> FENCES = new Long2ObjectOpenHashMap<>();
  private static long nextFence = 1L;

  private UniversalCraftVulkan() {
  }

  public static boolean isZZeroToOne() {
    return VulkanActive.zZeroToOne();
  }

  public static int maxTextureSize() {
    return RenderSystem.getDevice().getMaxTextureSize();
  }

  public static TextureFormat textureFormat(int glInternalFormat) {
    return switch (glInternalFormat) {
      case 0x8058 -> TextureFormat.RGBA8;
      case 0x8229 -> TextureFormat.RED8;
      case 0x8231 -> TextureFormat.RED8I;
      case 0x81A7, 0x8CAC -> TextureFormat.DEPTH32;
      default -> throw new UnsupportedOperationException(
        "UniversalCraft texture format 0x" + Integer.toHexString(glInternalFormat) + " has no Vulkan mapping on 26.1");
    };
  }

  public static void clearColor(GpuTexture texture, float red, float green, float blue, float alpha) {
    VulkanDevice.active.createCommandEncoder().clearColorTexture(texture, new Vector4f(red, green, blue, alpha));
  }

  public static void clearDepth(GpuTexture texture, double depth) {
    VulkanDevice.active.createCommandEncoder().clearDepthTexture(texture, depth);
  }

  public static void copyBufferToTexture(
    GpuBuffer source, long sourceOffset, long sourceSize, int sourceX, int sourceY, int sourceWidth, int sourceHeight,
    GpuTexture destination, int destinationX, int destinationY, int copyWidth, int copyHeight, int mipLevel, int arrayLayer
  ) {
    VulkanDevice.active.createCommandEncoder().copyBufferToTexture(
      source.slice(sourceOffset, sourceSize), sourceX, sourceY, sourceWidth, sourceHeight,
      destination, destinationX, destinationY, copyWidth, copyHeight, mipLevel, arrayLayer);
  }

  public static Long fenceCreate() {
    long id = nextFence++;
    FENCES.put(id, VulkanDevice.active.createCommandEncoder().createFence());
    return id;
  }

  public static int fenceWait(long id, long timeoutNs) {
    GpuFence fence = FENCES.get(id);
    return fence == null || fence.awaitCompletion(timeoutNs) ? GL_ALREADY_SIGNALED : GL_TIMEOUT_EXPIRED;
  }

  public static void fenceDelete(long id) {
    GpuFence fence = FENCES.remove(id);
    if (fence != null) fence.close();
  }
}
