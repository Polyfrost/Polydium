package org.polyfrost.polydium.vulkan;

import org.polyfrost.polydium.client.perf.PresentThread;
import org.polyfrost.polydium.vulkan.compat.VkMappedView;
import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;

public final class AsyncParticlesVulkan {
  private AsyncParticlesVulkan() {
  }

  private static VulkanCommandEncoder encoder() {
    return VulkanDevice.active.createCommandEncoder();
  }

  public static final class Vulkan {
    public static VkDevice getVkDevice() {
      return VulkanDevice.active.vkDevice();
    }

    public static void waitIdle() {
      PresentThread.drain();
      VK10.vkDeviceWaitIdle(VulkanDevice.active.vkDevice());
    }
  }

  public static final class DeviceManager {
    public static VkDevice vkDevice = VulkanDevice.active.vkDevice();
  }

  public interface MainPass {
    void rebindMainTarget();
  }

  public interface RendererAddon {
    long asyncparticles$getActualFrame();

    long asyncparticles$getInFlightFence(long frame);
  }

  public static final class Renderer implements MainPass, RendererAddon {
    private static final Renderer INSTANCE = new Renderer();

    public static Renderer getInstance() {
      return INSTANCE;
    }

    public static int getFramesNum() {
      return 2;
    }

    public static boolean isRecording() {
      return true;
    }

    public static VkCommandBuffer getCommandBuffer() {
      return encoder().commandBuffer();
    }

    public void endRenderPass() {
    }

    public MainPass getMainPass() {
      return this;
    }

    @Override
    public void rebindMainTarget() {
    }

    @Override
    public long asyncparticles$getActualFrame() {
      return encoder().currentSubmitIndex();
    }

    @Override
    public long asyncparticles$getInFlightFence(long frame) {
      return frame;
    }
  }

  public static final class Synchronization {
    public static void waitFence(long submitIndex) {
      if (!encoder().awaitSubmitCompletion(submitIndex, 5000000000L))
        throw new IllegalStateException("Timeout waiting for GPU particle slot submit: " + submitIndex);
    }
  }

  public static final class MemoryType {
  }

  public static final class MemoryTypes {
    public static MemoryType GPU_MEM = new MemoryType();
  }

  public static final class Buffer {
    long id, allocation, size;

    public Buffer(String name, int usage, MemoryType type) {
    }

    public long getId() {
      return this.id;
    }
  }

  public static final class MemoryManager {
    private static final MemoryManager INSTANCE = new MemoryManager();

    public static MemoryManager getInstance() {
      return INSTANCE;
    }

    public void createBuffer(Buffer buffer, long size, int usage, int memoryProperties) {
      try (MemoryStack stack = MemoryStack.stackPush()) {
        VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack).sType$Default().size(size).usage(usage).sharingMode(0);
        VmaAllocationCreateInfo allocationInfo = VmaAllocationCreateInfo.calloc(stack).usage(1).preferredFlags(memoryProperties);
        LongBuffer bufferPointer = stack.callocLong(1);
        PointerBuffer allocationPointer = stack.callocPointer(1);
        int result = Vma.vmaCreateBuffer(VulkanDevice.active.vma(), bufferInfo, allocationInfo, bufferPointer, allocationPointer, null);
        if (result != VK10.VK_SUCCESS)
          throw new IllegalStateException("vmaCreateBuffer failed for GPU particle buffer: " + result);
        buffer.id = bufferPointer.get(0);
        buffer.allocation = allocationPointer.get(0);
        buffer.size = size;
      }
    }
  }

  public static final class VkGpuBuffer extends VulkanGpuBuffer {
    private final Buffer buffer;
    private boolean closed;

    private VkGpuBuffer(Buffer buffer) {
      super(buffer.id, USAGE_VERTEX, buffer.size);
      this.buffer = buffer;
    }

    public static VkGpuBuffer from(Buffer buffer) {
      return new VkGpuBuffer(buffer);
    }

    public Buffer getBuffer() {
      return this.buffer;
    }

    @Override
    public void destroy() {
      Vma.vmaDestroyBuffer(VulkanDevice.active.vma(), this.vkBuffer(), this.buffer.allocation);
    }

    @Override
    public boolean isClosed() {
      return this.closed;
    }

    @Override
    public void close() {
      if (!this.closed) {
        this.closed = true;
        encoder().queueForDestroy(this);
      }
    }

    @Override
    public VkMappedView map(long offset, long length, boolean read, boolean write) {
      throw new UnsupportedOperationException();
    }
  }
}
