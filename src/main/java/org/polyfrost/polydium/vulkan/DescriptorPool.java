package org.polyfrost.polydium.vulkan;

import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

// Ported from 26.4-snapshot-1. Only used when VK_KHR_push_descriptor is unavailable
public class DescriptorPool implements Destroyable {
  private final VulkanDevice device;
  private final long poolHandle;

  public DescriptorPool(final VulkanDevice device) {
    this.device = device;

    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkDescriptorPoolSize.Buffer sizes = VkDescriptorPoolSize.calloc(3, stack);
      sizes.get(0).type(6).descriptorCount(4096);
      sizes.get(1).type(1).descriptorCount(4096);
      sizes.get(2).type(4).descriptorCount(128);
      VkDescriptorPoolCreateInfo createInfo = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(1024).pPoolSizes(sizes);
      LongBuffer pointer = stack.callocLong(1);
      VulkanUtils.crashIfFailure(device, VK12.vkCreateDescriptorPool(device.vkDevice(), createInfo, null, pointer), "Failed to allocate descriptor pool");
      this.poolHandle = pointer.get(0);
    }
  }

  @Override
  public void destroy() {
    VK12.vkDestroyDescriptorPool(this.device.vkDevice(), this.poolHandle, null);
  }

  public void reset() {
    VulkanUtils.crashIfFailure(this.device, VK12.vkResetDescriptorPool(this.device.vkDevice(), this.poolHandle, 0), "Failed to reset descriptor pool");
  }

  // 0 when the pool is exhausted or fragmented
  public long allocateSet(final long setLayout) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default()
        .descriptorPool(this.poolHandle)
        .pSetLayouts(stack.longs(setLayout));
      LongBuffer pointer = stack.callocLong(1);
      int result = VK12.vkAllocateDescriptorSets(this.device.vkDevice(), allocateInfo, pointer);
      if (result == VK12.VK_ERROR_OUT_OF_POOL_MEMORY || result == VK12.VK_ERROR_FRAGMENTED_POOL) {
        return 0L;
      }

      VulkanUtils.crashIfFailure(this.device, result, "Failed to allocate descriptor set");
      return pointer.get(0);
    }
  }
}
