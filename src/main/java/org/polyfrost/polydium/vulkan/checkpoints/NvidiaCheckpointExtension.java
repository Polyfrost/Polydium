package org.polyfrost.polydium.vulkan.checkpoints;

import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanQueue;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.NVDeviceDiagnosticCheckpoints;
import org.lwjgl.vulkan.VkCheckpointData2NV;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCheckpointData2NV.Buffer;

public class NvidiaCheckpointExtension implements CheckpointExtension {
  private final List<NvidiaCheckpointExtension.NvidiaCheckpointStorage> storages = new ArrayList<>();

  public NvidiaCheckpointExtension() {
  }

  @Override
  public CheckpointExtension.CheckpointStorage createStorage(final VulkanDevice device, final VulkanQueue queue, final int maxFramesInFlight) {
    NvidiaCheckpointExtension.NvidiaCheckpointStorage storage = new NvidiaCheckpointExtension.NvidiaCheckpointStorage(queue, maxFramesInFlight);
    this.storages.add(storage);
    return storage;
  }

  @Override
  public List<CheckpointExtension.QueueCheckpoints> retrieveCheckpoints(final boolean isDeviceLost) {
    if (!isDeviceLost) {
      return List.of();
    } else {
      List<CheckpointExtension.QueueCheckpoints> result = new ArrayList<>(this.storages.size());

      for (NvidiaCheckpointExtension.NvidiaCheckpointStorage storage : this.storages) {
        result.add(storage.retrieveCheckpoints());
      }

      return result;
    }
  }

  @Override
  public void close() {
  }

  private static class NvidiaCheckpointStorage extends AbstractCheckpointStorage {
    protected NvidiaCheckpointStorage(final VulkanQueue queue, final int maxFramesInFlight) {
      super(queue, maxFramesInFlight);
    }

    @Override
    protected void recordCheckpoint(final VkCommandBuffer commandBuffer, final int id) {
      NVDeviceDiagnosticCheckpoints.vkCmdSetCheckpointNV(commandBuffer, id);
    }

    public CheckpointExtension.QueueCheckpoints retrieveCheckpoints() {
      List<CheckpointExtension.StageCheckpoint> stageCheckpoints = new ArrayList<>();
      MemoryStack stack = MemoryStack.stackPush();

      try {
        IntBuffer count = stack.callocInt(1);
        //? if >= 26.1 {
        NVDeviceDiagnosticCheckpoints.vkGetQueueCheckpointData2NV(this.queue, count, null);
        Buffer data = VkCheckpointData2NV.calloc(count.get(0), stack);

        for (int i = 0; i < count.get(0); i++) {
          ((VkCheckpointData2NV)data.get(i)).sType$Default();
        }

        NVDeviceDiagnosticCheckpoints.vkGetQueueCheckpointData2NV(this.queue, count, data);

        while (data.remaining() > 0) {
          VkCheckpointData2NV checkpointData = (VkCheckpointData2NV)data.get();
          AbstractCheckpointStorage.Checkpoint checkpoint = this.findCheckpoint((int)checkpointData.pCheckpointMarker());
          if (checkpoint != null) {
            stageCheckpoints.add(new CheckpointExtension.StageCheckpoint(checkpointData.stage(), checkpoint.type(), checkpoint.label()));
          }
        }
        //?} else {
        /*NVDeviceDiagnosticCheckpoints.vkGetQueueCheckpointDataNV(this.queue, count, null);
        org.lwjgl.vulkan.VkCheckpointDataNV.Buffer data = org.lwjgl.vulkan.VkCheckpointDataNV.calloc(count.get(0), stack);

        for (int i = 0; i < count.get(0); i++) {
          ((org.lwjgl.vulkan.VkCheckpointDataNV)data.get(i)).sType$Default();
        }

        NVDeviceDiagnosticCheckpoints.vkGetQueueCheckpointDataNV(this.queue, count, data);

        while (data.remaining() > 0) {
          org.lwjgl.vulkan.VkCheckpointDataNV checkpointData = (org.lwjgl.vulkan.VkCheckpointDataNV)data.get();
          AbstractCheckpointStorage.Checkpoint checkpoint = this.findCheckpoint((int)checkpointData.pCheckpointMarker());
          if (checkpoint != null) {
            stageCheckpoints.add(new CheckpointExtension.StageCheckpoint(checkpointData.stage(), checkpoint.type(), checkpoint.label()));
          }
        }
        *///?}
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

      return new CheckpointExtension.QueueCheckpoints(this.queue.address(), stageCheckpoints);
    }
  }
}
