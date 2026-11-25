package org.polyfrost.polydium.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferMemoryBarrier;
import org.lwjgl.vulkan.VkBufferMemoryBarrier2;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageMemoryBarrier2;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryBarrier2;

public final class VulkanSync {
  public static boolean sync2 = true;

  private VulkanSync() {
  }

  public static void pipelineBarrier(final VkCommandBuffer commandBuffer, final VkDependencyInfo info) {
    if (sync2) {
      KHRSynchronization2.vkCmdPipelineBarrier2KHR(commandBuffer, info);
      return;
    }

    try (MemoryStack stack = MemoryStack.stackPush()) {
      long srcStages = 0L;
      long dstStages = 0L;
      VkMemoryBarrier.Buffer memory = null;
      VkBufferMemoryBarrier.Buffer buffers = null;
      VkImageMemoryBarrier.Buffer images = null;

      VkMemoryBarrier2.Buffer memory2 = info.pMemoryBarriers();
      if (memory2 != null) {
        memory = VkMemoryBarrier.calloc(memory2.remaining(), stack);
        for (int i = 0; i < memory2.remaining(); i++) {
          VkMemoryBarrier2 src = memory2.get(i);
          srcStages |= src.srcStageMask();
          dstStages |= src.dstStageMask();
          memory.get(i).sType$Default().srcAccessMask((int) src.srcAccessMask()).dstAccessMask((int) src.dstAccessMask());
        }
      }

      VkBufferMemoryBarrier2.Buffer buffers2 = info.pBufferMemoryBarriers();
      if (buffers2 != null) {
        buffers = VkBufferMemoryBarrier.calloc(buffers2.remaining(), stack);
        for (int i = 0; i < buffers2.remaining(); i++) {
          VkBufferMemoryBarrier2 src = buffers2.get(i);
          srcStages |= src.srcStageMask();
          dstStages |= src.dstStageMask();
          buffers.get(i).sType$Default()
            .srcAccessMask((int) src.srcAccessMask())
            .dstAccessMask((int) src.dstAccessMask())
            .srcQueueFamilyIndex(src.srcQueueFamilyIndex())
            .dstQueueFamilyIndex(src.dstQueueFamilyIndex())
            .buffer(src.buffer())
            .offset(src.offset())
            .size(src.size());
        }
      }

      VkImageMemoryBarrier2.Buffer images2 = info.pImageMemoryBarriers();
      if (images2 != null) {
        images = VkImageMemoryBarrier.calloc(images2.remaining(), stack);
        for (int i = 0; i < images2.remaining(); i++) {
          VkImageMemoryBarrier2 src = images2.get(i);
          srcStages |= src.srcStageMask();
          dstStages |= src.dstStageMask();
          images.get(i).sType$Default()
            .srcAccessMask((int) src.srcAccessMask())
            .dstAccessMask((int) src.dstAccessMask())
            .oldLayout(src.oldLayout())
            .newLayout(src.newLayout())
            .srcQueueFamilyIndex(src.srcQueueFamilyIndex())
            .dstQueueFamilyIndex(src.dstQueueFamilyIndex())
            .image(src.image())
            .subresourceRange(src.subresourceRange());
        }
      }

      VK10.vkCmdPipelineBarrier(
        commandBuffer,
        srcStages == 0L ? VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : (int) srcStages,
        dstStages == 0L ? VK10.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT : (int) dstStages,
        info.dependencyFlags(),
        memory,
        buffers,
        images
      );
    }
  }

  public static void writeTimestamp(final VkCommandBuffer commandBuffer, final long stage, final long queryPool, final int query) {
    if (sync2) {
      KHRSynchronization2.vkCmdWriteTimestamp2KHR(commandBuffer, stage, queryPool, query);
    } else {
      VK10.vkCmdWriteTimestamp(commandBuffer, stage == 0L ? VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : (int) stage, queryPool, query);
    }
  }
}
