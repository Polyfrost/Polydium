package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.GpuTexture;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public final class EssentialVulkan {
  private static Method[] copyOp;
  private static Method getUc;
  private static Method getMc;

  private EssentialVulkan() {
  }

  public static void registerMixins() {
    if (EssentialVulkan.class.getClassLoader().getResource("gg/essential/universal/render/UGpuDeviceImpl.class") == null) return;
    try {
      Object processor = IrisVulkanGate.processor();
      Mixins.addConfiguration("mixins.polydium.late.json");
      java.lang.reflect.Field count = processor.getClass().getDeclaredField("transformedCount");
      count.setAccessible(true);
      count.setInt(processor, 0);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Cannot register Essential's Vulkan patches", e);
    }
  }

  public static java.nio.ByteBuffer guiProjection(org.joml.Matrix4f matrix, java.nio.ByteBuffer buffer) {
    return (VulkanActive.active && VulkanActive.zZeroToOne() ? new org.joml.Matrix4f().translate(0.0F, 0.0F, 0.5F).scale(1.0F, 1.0F, 0.5F).mul(matrix) : matrix).get(buffer);
  }

  public static boolean isZZeroToOne(boolean essential) {
    return VulkanActive.active ? VulkanActive.zZeroToOne() : essential;
  }

  public static void clearColor(GpuTexture texture, byte red, byte green, byte blue, byte alpha) {
    VulkanDevice.active.createCommandEncoder().clearColorTexture(
      texture, new Vector4f(red & 255, green & 255, blue & 255, alpha & 255).div(255.0F));
  }

  public static void clearDepth(GpuTexture texture, float depth) {
    VulkanDevice.active.createCommandEncoder().clearDepthTexture(texture, depth);
  }

  public static void copyFrom(GpuTexture destination, Iterable<?> sources) {
    try {
      for (Object op : sources) {
        if (copyOp == null) {
          Class<?> type = op.getClass();
          ClassLoader loader = type.getClassLoader();
          getUc = Class.forName("gg.essential.util.image.GpuTexture", false, loader).getMethod("getUc");
          getMc = Class.forName("gg.essential.universal.render.UGpuTextureImpl", false, loader).getMethod("getMc");
          String[] names = {"getSrc", "getSrcX", "getSrcY", "getDestX", "getDestY", "getWidth", "getHeight"};
          Method[] getters = new Method[names.length];
          for (int i = 0; i < names.length; i++) getters[i] = type.getMethod(names[i]);
          copyOp = getters;
        }
        int[] v = new int[7];
        for (int i = 1; i < 7; i++) v[i] = (int) copyOp[i].invoke(op);
        GpuTexture source = (GpuTexture) getMc.invoke(getUc.invoke(copyOp[0].invoke(op)));
        VulkanDevice.active.createCommandEncoder().copyTextureToTexture(source, destination, 0, v[3], v[4], v[1], v[2], v[5], v[6]);
      }
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Essential GpuTexture.CopyOp changed shape", e);
    }
  }

  public static ByteBuffer readPixelColors(GpuTexture texture, int x, int y, int width, int height) {
    return read(texture, x, y, width, height, ByteBuffer.allocate(width * height * 4));
  }

  public static FloatBuffer readPixelDepths(GpuTexture texture, int x, int y, int width, int height) {
    return read(texture, x, y, width, height, BufferUtils.createByteBuffer(width * height * 4)).rewind().asFloatBuffer();
  }

  private static ByteBuffer read(GpuTexture texture, int x, int y, int width, int height, ByteBuffer out) {
    VulkanDevice device = VulkanDevice.active;
    GpuBuffer buffer = device.createBuffer(
      () -> "Essential readPixels buffer", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, (long) width * height * 4);
    try {
      VulkanCommandEncoder encoder = device.createCommandEncoder();
      encoder.copyTextureToBuffer(texture, buffer, 0L, () -> {
      }, 0, x, y, width, height);
      try (GpuFence fence = encoder.createFence()) {
        encoder.submit();
        fence.awaitCompletion(Long.MAX_VALUE);
      }
      try (GpuBuffer.MappedView view = encoder.mapBuffer(buffer.slice(), true, false)) {
        out.put(view.data());
      }
      return out;
    } finally {
      buffer.close();
    }
  }

  public static void upload(GpuTexture texture, ByteBuffer pixels, int width, int height) {
    VulkanDevice.active.createCommandEncoder().writeToTexture(texture, pixels, NativeImage.Format.RGBA, 0, 0, 0, 0, width, height);
  }
}
