package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
//? if >= 26.2 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanUtils;
//?} else {
/*import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanGpuBuffer;
import org.polyfrost.polydium.vulkan.VulkanUtils;
*///?}
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRPushDescriptor;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public final class VulkanDescriptorWrites {
    private static final int IMAGE_LAYOUT = 1;

    private VulkanDescriptorWrites() {
    }

    public static long writes(MemoryStack stack, int count) {
        return stack.ncalloc(VkWriteDescriptorSet.ALIGNOF, count, VkWriteDescriptorSet.SIZEOF);
    }

    public static long write(long writes, int binding) {
        long write = writes + (long) binding * VkWriteDescriptorSet.SIZEOF;
        VkWriteDescriptorSet.nsType(write, VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        VkWriteDescriptorSet.ndstBinding(write, binding);
        VkWriteDescriptorSet.ndescriptorCount(write, 1);
        return write;
    }

    public static void uniformBuffer(MemoryStack stack, long write, GpuBufferSlice slice) {
        long info = stack.ncalloc(VkDescriptorBufferInfo.ALIGNOF, 1, VkDescriptorBufferInfo.SIZEOF);
        VkDescriptorBufferInfo.nbuffer(info, ((VulkanGpuBuffer) slice.buffer()).vkBuffer());
        VkDescriptorBufferInfo.noffset(info, slice.offset());
        VkDescriptorBufferInfo.nrange(info, slice.length());
        VkWriteDescriptorSet.ndescriptorType(write, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        MemoryUtil.memPutAddress(write + VkWriteDescriptorSet.PBUFFERINFO, info);
    }

    public static void imageSampler(MemoryStack stack, long write, long sampler, long imageView) {
        long info = stack.ncalloc(VkDescriptorImageInfo.ALIGNOF, 1, VkDescriptorImageInfo.SIZEOF);
        VkDescriptorImageInfo.nsampler(info, sampler);
        VkDescriptorImageInfo.nimageView(info, imageView);
        VkDescriptorImageInfo.nimageLayout(info, IMAGE_LAYOUT);
        VkWriteDescriptorSet.ndescriptorType(write, VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
        MemoryUtil.memPutAddress(write + VkWriteDescriptorSet.PIMAGEINFO, info);
    }

    public static void texelBuffer(MemoryStack stack, long write, GpuBufferSlice slice, int format, VulkanDevice device, VulkanCommandEncoder encoder) {
        long view = stack.ncalloc(Long.BYTES, 1, Long.BYTES);
        MemoryUtil.memPutLong(view, texelView(stack, slice, format, device, encoder));
        VkWriteDescriptorSet.ndescriptorType(write, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER);
        MemoryUtil.memPutAddress(write + VkWriteDescriptorSet.PTEXELBUFFERVIEW, view);
    }

    public static void push(VkCommandBuffer commandBuffer, long pipelineLayout, int count, long writes) {
        KHRPushDescriptor.nvkCmdPushDescriptorSetKHR(commandBuffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, count, writes);
    }

    private static long texelView(MemoryStack stack, GpuBufferSlice slice, int format, VulkanDevice device, VulkanCommandEncoder encoder) {
        VulkanGpuBuffer buffer = (VulkanGpuBuffer) slice.buffer();
        TexelViewCache.Views views = buffer instanceof VulkanGpuBuffer.Direct && !buffer.isClosed() && buffer instanceof TexelViewCache cache
            ? cache.polydium$texelViews() : null;
        if (views != null) {
            long cached = views.find(slice.offset(), slice.length(), format);
            if (cached != 0L) {
                return cached;
            }
        }

        long view;
        try (MemoryStack frame = stack.push()) {
            long info = frame.ncalloc(VkBufferViewCreateInfo.ALIGNOF, 1, VkBufferViewCreateInfo.SIZEOF);
            long handle = frame.ncalloc(Long.BYTES, 1, Long.BYTES);
            VkBufferViewCreateInfo.nsType(info, VK10.VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO);
            VkBufferViewCreateInfo.nbuffer(info, buffer.vkBuffer());
            VkBufferViewCreateInfo.noffset(info, slice.offset());
            VkBufferViewCreateInfo.nrange(info, slice.length());
            VkBufferViewCreateInfo.nformat(info, format);
            VulkanUtils.crashIfFailure(device, VK10.nvkCreateBufferView(device.vkDevice(), info, MemoryUtil.NULL, handle), "Couldn't create buffer view for texel buffer");
            view = MemoryUtil.memGetLong(handle);
        }

        if (views != null && views.store(slice.offset(), slice.length(), format, view)) {
            return view;
        }
        long destroyed = view;
        encoder.queueForDestroy(() -> VK10.vkDestroyBufferView(device.vkDevice(), destroyed, null));
        return view;
    }
}
//?}
