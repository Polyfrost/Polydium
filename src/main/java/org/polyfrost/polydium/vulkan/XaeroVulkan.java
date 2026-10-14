package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.client.renderer.RenderPipelines;

public final class XaeroVulkan {
  private XaeroVulkan() {
  }

  public static void generateMipmaps(GpuTexture texture) {
    GpuDevice device = RenderSystem.getDevice();
    GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
    for (int mip = 1; mip < texture.getMipLevels(); mip++) {
      try (GpuTextureView source = device.createTextureView(texture, mip - 1, 1);
           GpuTextureView target = device.createTextureView(texture, mip, 1);
           RenderPass pass = device.createCommandEncoder().createRenderPass(() -> "Xaero mipmap", target, OptionalInt.empty())) {
        pass.setPipeline(RenderPipelines.TRACY_BLIT);
        RenderSystem.bindDefaultUniforms(pass);
        pass.bindTexture("InSampler", source, sampler);
        pass.draw(0, 3);
      }
    }
  }

  public static void copyBufferToTexture(GpuBuffer buffer, GpuTexture texture, int x, int y, int width, int height) {
    VulkanDevice.active.createCommandEncoder().copyBufferToTexture(buffer.slice(), x, y, width, height, texture, x, y, width, height, 0, 0);
  }

  public static void writeToTexture(GpuTexture texture, ByteBuffer colours, int width, int height) {
    RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, colours, NativeImage.Format.RGBA, 0, 0, 0, 0, width, height);
  }

  public static void submit() {
    VulkanDevice.active.createCommandEncoder().submit();
  }

  public static void finish() {
    try (GpuFence fence = RenderSystem.getDevice().createCommandEncoder().createFence()) {
      submit();
      fence.awaitCompletion(Long.MAX_VALUE);
    }
  }

  public static void downloadToBgra(GpuTexture texture, ByteBuffer out) {
    GpuDevice device = RenderSystem.getDevice();
    int size = texture.getWidth(0) * texture.getHeight(0) * 4;
    try (GpuBuffer buffer = device.createBuffer(() -> "Xaero export readback", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, size)) {
      device.createCommandEncoder().copyTextureToBuffer(texture, buffer, 0, () -> {}, 0);
      finish();
      try (GpuBuffer.MappedView view = device.createCommandEncoder().mapBuffer(buffer, true, false)) {
        ByteBuffer data = view.data();
        for (int i = 0; i < size; i += 4)
          out.put(i, data.get(i + 2)).put(i + 1, data.get(i + 1)).put(i + 2, data.get(i)).put(i + 3, data.get(i + 3));
      }
    }
  }

  public static ByteBuffer putColour(ByteBuffer buffer, int pos, int colour) {
    return buffer.put(pos, (byte)(colour >> 8)).put(pos + 1, (byte)(colour >> 16)).put(pos + 2, (byte)(colour >> 24)).put(pos + 3, (byte)colour);
  }

  public static int getColour(ByteBuffer buffer, int pos) {
    return (buffer.get(pos + 2) & 0xFF) << 24 | (buffer.get(pos + 1) & 0xFF) << 16 | (buffer.get(pos) & 0xFF) << 8 | buffer.get(pos + 3) & 0xFF;
  }

  public static int cacheVersion() {
    return VulkanActive.active ? 196633 : 131096;
  }

  public static int cacheMajor() {
    return VulkanActive.active ? 3 : 2;
  }

  public static int cacheMinor() {
    return VulkanActive.active ? 25 : 24;
  }

  public static boolean ignoreCache(int majorSaveVersion) {
    return VulkanActive.active && majorSaveVersion < 3;
  }
}
