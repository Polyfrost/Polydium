package org.polyfrost.oneconfig.internal.ui.services;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import kotlin.Pair;
import kotlin.Triple;
import net.minecraft.client.Minecraft;
import org.jetbrains.skia.BackendRenderTarget;
import org.jetbrains.skia.DirectContext;
import org.jetbrains.skia.SurfaceColorFormat;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
//? if >= 26.1
import org.polyfrost.oneconfig.internal.mixin.blaze3d.GpuDeviceAccessor;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanConst;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanGpuTexture;
import org.polyfrost.polydium.vulkan.VulkanQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NativeVulkanService implements VulkanService {
  private static final Logger LOG = LoggerFactory.getLogger(NativeVulkanService.class);

  private final long vkInstance;
  private final long vkPhysDevice;
  private final long vkDevice;
  private final long vkQueue;
  private final int queueFamilyIndex;

  private NativeVulkanService(final long vkInstance, final long vkPhysDevice, final long vkDevice, final long vkQueue, final int queueFamilyIndex) {
    this.vkInstance = vkInstance;
    this.vkPhysDevice = vkPhysDevice;
    this.vkDevice = vkDevice;
    this.vkQueue = vkQueue;
    this.queueFamilyIndex = queueFamilyIndex;
  }

  public static NativeVulkanService tryCreate() {
    try {
      final VulkanDevice device = device();
      if (device == null) {
        LOG.info("GpuDevice backend is not Polydium Vulkan; falling back to GL");
        return null;
      }
      final VkDevice vkDevice = device.vkDevice();
      final VulkanQueue queue = device.graphicsQueue();
      return new NativeVulkanService(
        device.instance().vkInstance().address(),
        vkDevice.getPhysicalDevice().address(),
        vkDevice.address(),
        queue.vkQueue().address(),
        queue.queueFamilyIndex()
      );
    } catch (final Exception e) {
      LOG.error("NativeVulkanService.tryCreate() failed", e);
      return null;
    }
  }

  private static VulkanDevice device() {
    final GpuDevice device = RenderSystem.tryGetDevice();
    //? if >= 26.1 {
    return device instanceof final GpuDeviceAccessor accessor && accessor.oneconfig$getBackend() instanceof final VulkanDevice vk ? vk : null;
    //?} else {
    /*return device instanceof final VulkanDevice vk ? vk : null;
    *///?}
  }

  private static VulkanGpuTexture color(final RenderTarget target) {
    return target.getColorTexture() instanceof final VulkanGpuTexture tex ? tex : null;
  }

  @Override
  public boolean isVulkan() {
    return true;
  }

  @Override
  public boolean getOffscreenNeedsPerFrameRewrap() {
    return true;
  }

  //? if >= 26.1
  @Override
  public boolean getMainRTRestoreNeedsColorPass() {
    return true;
  }

  @Override
  public DirectContext makeDirectContext() {
    final FunctionProvider provider = VK.getFunctionProvider();
    return DirectContext.Companion.makeVulkan(
      this.vkInstance,
      this.vkPhysDevice,
      this.vkDevice,
      this.vkQueue,
      this.queueFamilyIndex,
      provider.getFunctionAddress("vkGetInstanceProcAddr"),
      provider.getFunctionAddress("vkGetDeviceProcAddr"),
      VK12.VK_API_VERSION_1_2,
      null
    );
  }

  @Override
  public BackendRenderTarget makeBackendRenderTarget(final int width, final int height, final long vkImageHandle, final int vkFormat, final int vkQueueFamily) {
    return BackendRenderTarget.Companion.makeVulkan(width, height, vkImageHandle, 0, 1, vkFormat, 23, 1, 1);
  }

  @Override
  public BackendRenderTarget makeBackBufferRenderTarget(final int width, final int height, final long vkImageHandle, final int vkFormat, final int vkQueueFamily) {
    return this.makeBackendRenderTarget(width, height, vkImageHandle, vkFormat, vkQueueFamily);
  }

  @Override
  public Pair<BackendRenderTarget, SurfaceColorFormat> makeOffscreenBRT(final RenderTarget target, final int width, final int height) {
    final VulkanGpuTexture tex = color(target);
    if (tex == null) {
      throw new IllegalStateException("Expected VulkanGpuTexture on offscreen TextureTarget, got " + target.getColorTexture());
    }
    return new Pair<>(
      this.makeBackendRenderTarget(width, height, tex.vkImage(), VulkanConst.toVk(tex.gpuFormat()), this.queueFamilyIndex),
      SurfaceColorFormat.RGBA_8888
    );
  }

  @Override
  public Triple<Long, Integer, Integer> getMainColorImageInfo() {
    try {
      final VulkanGpuTexture tex = color(Minecraft.getInstance().getMainRenderTarget());
      if (tex != null) {
        return new Triple<>(tex.vkImage(), VulkanConst.toVk(tex.gpuFormat()), this.queueFamilyIndex);
      }
    } catch (final Exception e) {
      LOG.warn("getMainColorImageInfo failed", e);
    }
    return new Triple<>(0L, 0, 0);
  }

  @Override
  public void restoreMainRTLayout() {
    this.transitionImage(color(Minecraft.getInstance().getMainRenderTarget()), 2, 1, 1024, 65536, 256, 98304);
  }

  //? if >= 26.1
  @Override
  public void transitionSkiaTargetForSampling(final RenderTarget target) {
    this.transitionImage(color(target), 2, 1, 1024, 128, 256, 32);
  }

  //? if >= 26.1
  @Override
  public void transitionSkiaTargetForRendering(final RenderTarget target) {
    this.transitionImage(color(target), 1, 2, 128, 1024, 32, 384);
    final VulkanDevice device = device();
    if (device != null) {
      device.createCommandEncoder().submit();
    }
  }

  @Override
  public void transitionOffscreenForSampling(final RenderTarget target) {
    this.transitionImage(color(target), 1, 1, 1024, 128, 256, 32);
  }

  @Override
  public void transitionOffscreenForRendering(final RenderTarget target) {
    this.transitionImage(color(target), 1, 1, 1152, 1024, 288, 384);
  }

  @Override
  public void midFrameFlush() {
    final VulkanDevice device = device();
    if (device == null) {
      LOG.warn("midFrameFlush: GpuDevice backend is not Vulkan, skipping flush");
      return;
    }
    final VulkanCommandEncoder encoder = device.createCommandEncoder();
    try {
      final VkCommandBuffer cmd = encoder.allocateAndBeginTransientCommandBuffer();
      try (MemoryStack stack = MemoryStack.stackPush()) {
        VulkanCommandEncoder.memoryBarrier(cmd, stack);
      } finally {
        VK12.vkEndCommandBuffer(cmd);
        encoder.execute(cmd);
      }
    } catch (final Exception e) {
      LOG.warn("midFrameFlush: failed to record the flush barrier, skipping submit", e);
      return;
    }
    encoder.submit();
  }

  private void transitionImage(final VulkanGpuTexture tex, final int oldLayout, final int newLayout, final int srcStageMask, final int dstStageMask, final int srcAccessMask, final int dstAccessMask) {
    final VulkanDevice device = device();
    if (tex == null || device == null) {
      return;
    }
    try {
      final VulkanCommandEncoder encoder = device.createCommandEncoder();
      final VkCommandBuffer cmd = encoder.allocateAndBeginTransientCommandBuffer();
      try (MemoryStack stack = MemoryStack.stackPush()) {
        final VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack)
          .sType$Default()
          .oldLayout(oldLayout)
          .newLayout(newLayout)
          .srcAccessMask(srcAccessMask)
          .dstAccessMask(dstAccessMask)
          .srcQueueFamilyIndex(VK12.VK_QUEUE_FAMILY_IGNORED)
          .dstQueueFamilyIndex(VK12.VK_QUEUE_FAMILY_IGNORED)
          .image(tex.vkImage());
        barrier.subresourceRange().aspectMask(VK12.VK_IMAGE_ASPECT_COLOR_BIT).baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);
        VK12.vkCmdPipelineBarrier(cmd, srcStageMask, dstStageMask, 0, null, null, barrier);
      } finally {
        VK12.vkEndCommandBuffer(cmd);
        encoder.execute(cmd);
      }
    } catch (final Exception e) {
      LOG.warn("transitionImage failed", e);
    }
  }
}
