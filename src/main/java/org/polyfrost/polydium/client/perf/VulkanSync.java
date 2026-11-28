package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferMemoryBarrier;
import org.lwjgl.vulkan.VkBufferMemoryBarrier2;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageMemoryBarrier2;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryBarrier2;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubmitInfo2;
import org.lwjgl.vulkan.VkTimelineSemaphoreSubmitInfo;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

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

    public static int queueSubmit(final VkQueue queue, final VkSubmitInfo2.Buffer submits2, final long fence) {
        if (sync2) {
            return KHRSynchronization2.vkQueueSubmit2KHR(queue, submits2, fence);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            int count = submits2.remaining();
            VkSubmitInfo.Buffer submits = VkSubmitInfo.calloc(count, stack);
            for (int i = 0; i < count; i++) {
                VkSubmitInfo2 src = submits2.get(submits2.position() + i);
                VkSubmitInfo submit = submits.get(i).sType$Default();
                VkTimelineSemaphoreSubmitInfo timeline = VkTimelineSemaphoreSubmitInfo.calloc(stack).sType$Default();
                submit.pNext(timeline);

                VkSemaphoreSubmitInfo.Buffer waits = src.pWaitSemaphoreInfos();
                int waitCount = waits == null ? 0 : waits.remaining();
                LongBuffer waitSemaphores = stack.mallocLong(waitCount);
                LongBuffer waitValues = stack.mallocLong(waitCount);
                IntBuffer waitStages = stack.mallocInt(waitCount);
                for (int j = 0; j < waitCount; j++) {
                    VkSemaphoreSubmitInfo wait = waits.get(j);
                    waitSemaphores.put(j, wait.semaphore());
                    waitValues.put(j, wait.value());
                    waitStages.put(j, wait.stageMask() == 0L ? VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : (int) wait.stageMask());
                }
                submit.waitSemaphoreCount(waitCount);
                submit.pWaitSemaphores(waitSemaphores);
                submit.pWaitDstStageMask(waitStages);
                timeline.waitSemaphoreValueCount(waitCount);
                timeline.pWaitSemaphoreValues(waitValues);

                VkCommandBufferSubmitInfo.Buffer buffers = src.pCommandBufferInfos();
                int bufferCount = buffers == null ? 0 : buffers.remaining();
                PointerBuffer commandBuffers = stack.mallocPointer(bufferCount);
                for (int j = 0; j < bufferCount; j++) {
                    commandBuffers.put(j, buffers.get(j).commandBuffer());
                }
                submit.pCommandBuffers(commandBuffers);

                VkSemaphoreSubmitInfo.Buffer signals = src.pSignalSemaphoreInfos();
                int signalCount = signals == null ? 0 : signals.remaining();
                LongBuffer signalSemaphores = stack.mallocLong(signalCount);
                LongBuffer signalValues = stack.mallocLong(signalCount);
                for (int j = 0; j < signalCount; j++) {
                    VkSemaphoreSubmitInfo signal = signals.get(j);
                    signalSemaphores.put(j, signal.semaphore());
                    signalValues.put(j, signal.value());
                }
                submit.pSignalSemaphores(signalSemaphores);
                timeline.signalSemaphoreValueCount(signalCount);
                timeline.pSignalSemaphoreValues(signalValues);
            }

            return VK10.vkQueueSubmit(queue, submits, fence);
        }
    }
}
//?}
