package org.polyfrost.polydium.vulkan.checkpoints;

import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanQueue;
import java.util.List;
import java.util.function.Supplier;
import org.lwjgl.vulkan.VkCommandBuffer;

public interface CheckpointExtension extends AutoCloseable {
  CheckpointExtension.CheckpointStorage createStorage(VulkanDevice device, VulkanQueue queue, int maxFramesInFlight);

  List<CheckpointExtension.QueueCheckpoints> retrieveCheckpoints(boolean isDeviceLost);

  @Override
  void close();

  public interface CheckpointStorage {
    void rotate();

    void recordCheckpoint(VkCommandBuffer commandBuffer, CheckpointExtension.CheckpointType type, Supplier<String> label);
  }

  public static enum CheckpointType {
    BEGIN_RENDER_PASS,
    END_RENDER_PASS;

    private CheckpointType() {
    }
  }

  public record QueueCheckpoints(long queue, List<CheckpointExtension.StageCheckpoint> checkpoints) {
  }

  public record StageCheckpoint(long stage, CheckpointExtension.CheckpointType type, String label) {
  }
}
