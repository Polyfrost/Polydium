package org.polyfrost.polydium.vulkan;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.Objects;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubmitInfo2;
import org.lwjgl.vulkan.VkTimelineSemaphoreSubmitInfo;
import org.lwjgl.vulkan.VkSubmitInfo2.Buffer;

public record VulkanQueue(VkQueue vkQueue, int queueFamilyIndex) {
  public VulkanQueue(final VulkanDevice device, final int queueFamilyIndex, final int queueIndex) {
    this(new VkQueue(fetchVkQueue(device, queueFamilyIndex, queueIndex), device.vkDevice()), queueFamilyIndex);
  }

  private static long fetchVkQueue(final VulkanDevice device, final int queueFamilyIndex, final int queueIndex) {
    MemoryStack stack = MemoryStack.stackPush();

    long var5;
    try {
      PointerBuffer queueHandlePtr = stack.callocPointer(1);
      VK12.vkGetDeviceQueue(device.vkDevice(), queueFamilyIndex, queueIndex, queueHandlePtr);
      var5 = queueHandlePtr.get(0);
    } catch (Throwable var8) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var7) {
          var8.addSuppressed(var7);
        }
      }

      throw var8;
    }

    if (stack != null) {
      stack.close();
    }

    return var5;
  }

  public VulkanQueue.Submission beginSubmit() {
    return new VulkanQueue.Submission();
  }

  public void waitIdle() {
    VK12.vkQueueWaitIdle(this.vkQueue);
  }

  public class Submission implements AutoCloseable {
    private boolean closed;
    private final ReferenceArrayList<VulkanQueue.Submission.SubmitStage> stages;

    private Submission() {
      super();
      this.closed = false;
      this.stages = new ReferenceArrayList();
      this.stages.add(new VulkanQueue.Submission.SubmitStage());
    }

    public void waitSemaphore(final long semaphore, final long value, final long stageMask) {
      if (this.closed) {
        throw new IllegalStateException("Attempt to use closed Submission");
      } else {
        if (!((VulkanQueue.Submission.SubmitStage)this.stages.top()).commandBuffers.isEmpty()
          || !((VulkanQueue.Submission.SubmitStage)this.stages.top()).signals.isEmpty()) {
          this.stages.add(new VulkanQueue.Submission.SubmitStage());
        }

        ((VulkanQueue.Submission.SubmitStage)this.stages.top()).waits.add(new VulkanQueue.Submission.SemaphoreOp(semaphore, value, stageMask));
      }
    }

    public void executeCommands(final VkCommandBuffer commandBuffer) {
      if (this.closed) {
        throw new IllegalStateException("Attempt to use closed Submission");
      } else {
        if (!((VulkanQueue.Submission.SubmitStage)this.stages.top()).signals.isEmpty()) {
          this.stages.add(new VulkanQueue.Submission.SubmitStage());
        }

        ((VulkanQueue.Submission.SubmitStage)this.stages.top()).commandBuffers.add(commandBuffer);
      }
    }

    public void signalSemaphore(final long semaphore, final long value, final long stageMask) {
      if (this.closed) {
        throw new IllegalStateException("Attempt to use closed Submission");
      } else {
        ((VulkanQueue.Submission.SubmitStage)this.stages.top()).signals.add(new VulkanQueue.Submission.SemaphoreOp(semaphore, value, stageMask));
      }
    }

    @Override
    public void close() {
      if (!VulkanSync.sync2) {
        this.submitLegacy();
        return;
      }

      MemoryStack stack = MemoryStack.stackPush();

      try {
        Buffer submits = VkSubmitInfo2.calloc(this.stages.size(), stack);

        for (int i = 0; i < this.stages.size(); i++) {
          VulkanQueue.Submission.SubmitStage stage = (VulkanQueue.Submission.SubmitStage)this.stages.get(i);
          ((Buffer)submits.position(i)).sType$Default();
          if (!stage.waits.isEmpty()) {
            org.lwjgl.vulkan.VkSemaphoreSubmitInfo.Buffer waits = VkSemaphoreSubmitInfo.calloc(stage.waits.size(), stack);
            submits.pWaitSemaphoreInfos(waits);

            for (int j = 0; j < stage.waits.size(); j++) {
              VulkanQueue.Submission.SemaphoreOp wait = stage.waits.get(j);
              ((org.lwjgl.vulkan.VkSemaphoreSubmitInfo.Buffer)waits.position(j)).sType$Default();
              waits.semaphore(wait.vkSemaphore);
              waits.value(wait.value);
              waits.stageMask(wait.stageMask);
            }
          }

          if (!stage.commandBuffers.isEmpty()) {
            org.lwjgl.vulkan.VkCommandBufferSubmitInfo.Buffer buffers = VkCommandBufferSubmitInfo.calloc(stage.commandBuffers.size(), stack);
            submits.pCommandBufferInfos(buffers);

            for (int j = 0; j < stage.commandBuffers.size(); j++) {
              VkCommandBuffer vkCommandBuffer = stage.commandBuffers.get(j);
              ((org.lwjgl.vulkan.VkCommandBufferSubmitInfo.Buffer)buffers.position(j)).sType$Default();
              buffers.commandBuffer(vkCommandBuffer);
            }
          }

          if (!stage.signals.isEmpty()) {
            org.lwjgl.vulkan.VkSemaphoreSubmitInfo.Buffer signals = VkSemaphoreSubmitInfo.calloc(stage.signals.size(), stack);
            submits.pSignalSemaphoreInfos(signals);

            for (int j = 0; j < stage.signals.size(); j++) {
              VulkanQueue.Submission.SemaphoreOp signal = stage.signals.get(j);
              ((org.lwjgl.vulkan.VkSemaphoreSubmitInfo.Buffer)signals.position(j)).sType$Default();
              signals.semaphore(signal.vkSemaphore);
              signals.value(signal.value);
              signals.stageMask(signal.stageMask);
            }
          }
        }

        submits.position(0);
        KHRSynchronization2.vkQueueSubmit2KHR(VulkanQueue.this.vkQueue, submits, 0L);
        this.closed = true;
      } catch (Throwable var9) {
        if (stack != null) {
          try {
            stack.close();
          } catch (Throwable var8) {
            var9.addSuppressed(var8);
          }
        }

        throw var9;
      }

      if (stack != null) {
        stack.close();
      }
    }

    private void submitLegacy() {
      try (MemoryStack stack = MemoryStack.stackPush()) {
        VkSubmitInfo.Buffer submits = VkSubmitInfo.calloc(this.stages.size(), stack);

        for (int i = 0; i < this.stages.size(); i++) {
          VulkanQueue.Submission.SubmitStage stage = this.stages.get(i);
          VkSubmitInfo submit = submits.get(i).sType$Default();
          VkTimelineSemaphoreSubmitInfo timeline = VkTimelineSemaphoreSubmitInfo.calloc(stack).sType$Default();
          submit.pNext(timeline);

          int waitCount = stage.waits.size();
          LongBuffer waitSemaphores = stack.mallocLong(waitCount);
          LongBuffer waitValues = stack.mallocLong(waitCount);
          IntBuffer waitStages = stack.mallocInt(waitCount);
          for (SemaphoreOp wait : stage.waits) {
            waitSemaphores.put(wait.vkSemaphore);
            waitValues.put(wait.value);
            waitStages.put(wait.stageMask == 0L ? VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : (int) wait.stageMask);
          }
          submit.waitSemaphoreCount(waitCount);
          submit.pWaitSemaphores(waitSemaphores.flip());
          submit.pWaitDstStageMask(waitStages.flip());
          timeline.waitSemaphoreValueCount(waitCount);
          timeline.pWaitSemaphoreValues(waitValues.flip());

          PointerBuffer commandBuffers = stack.mallocPointer(stage.commandBuffers.size());
          for (VkCommandBuffer commandBuffer : stage.commandBuffers) {
            commandBuffers.put(commandBuffer);
          }
          submit.pCommandBuffers(commandBuffers.flip());

          int signalCount = stage.signals.size();
          LongBuffer signalSemaphores = stack.mallocLong(signalCount);
          LongBuffer signalValues = stack.mallocLong(signalCount);
          for (SemaphoreOp signal : stage.signals) {
            signalSemaphores.put(signal.vkSemaphore);
            signalValues.put(signal.value);
          }
          submit.pSignalSemaphores(signalSemaphores.flip());
          timeline.signalSemaphoreValueCount(signalCount);
          timeline.pSignalSemaphoreValues(signalValues.flip());
        }

        VK10.vkQueueSubmit(VulkanQueue.this.vkQueue, submits, 0L);
        this.closed = true;
      }
    }

    private record SemaphoreOp(long vkSemaphore, long value, long stageMask) {
    }

    private record SubmitStage(
      List<VulkanQueue.Submission.SemaphoreOp> waits, List<VkCommandBuffer> commandBuffers, List<VulkanQueue.Submission.SemaphoreOp> signals
    ) {
      public SubmitStage() {
        this(new ReferenceArrayList(), new ReferenceArrayList(), new ReferenceArrayList());
      }
    }
  }
}
