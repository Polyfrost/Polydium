package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkAttachmentDescription2;
import org.lwjgl.vulkan.VkAttachmentReference2;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo2;
import org.lwjgl.vulkan.VkSubpassBeginInfo;
import org.lwjgl.vulkan.VkSubpassDescription2;
import org.lwjgl.vulkan.VkSubpassEndInfo;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

// 26.4-snapshot-1 dropped VK_KHR_dynamic_rendering and VK_KHR_push_descriptor for VkRenderPass + VkFramebuffer per pass and
// descriptor sets from per-submit pools. Ported as the fallback for devices without them; both are still used when present
public final class VulkanLegacyRendering {
    // Forces the fallback on capable hardware, for testing
    public static final boolean FORCE = Boolean.getBoolean("polydium.vulkan.legacyRenderPasses");

    public interface Device {
        boolean polydium$dynamicRendering();

        boolean polydium$pushDescriptors();

        RenderPassCache polydium$renderPassCache();
    }

    public interface Encoder {
        long polydium$allocateDescriptorSet(long setLayout);
    }

    public interface Pipeline {
        long polydium$descriptorSetLayout();
    }

    private VulkanLegacyRendering() {
    }

    public static void beginRenderPass(VulkanDevice device, VulkanCommandEncoder encoder, VkCommandBuffer commandBuffer, RenderPassDescriptor descriptor, VkRect2D renderArea) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            List<RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colors = descriptor.colorAttachments();
            RenderPassDescriptor.Attachment<OptionalDouble> depth = descriptor.depthAttachment();
            LongBuffer views = stack.mallocLong(colors.size() + 1);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(colors.size() + 1, stack);
            for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> color : colors) {
                if (color != null) {
                    views.put(((VulkanGpuTextureView) color.textureView()).vkImageView());
                    VkClearValue clearValue = clearValues.get();
                    color.clearValue().ifPresent(value -> VulkanUtils.putArgb(clearValue.color(), value));
                }
            }
            if (depth != null) {
                views.put(((VulkanGpuTextureView) depth.textureView()).vkImageView());
                VkClearValue clearValue = clearValues.get();
                depth.clearValue().ifPresent(value -> clearValue.depthStencil().depth((float) value));
            }

            long renderPass = ((Device) device).polydium$renderPassCache().get(descriptor);
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack).sType$Default()
                .renderPass(renderPass)
                .pAttachments(views.flip())
                .width(VulkanDeferral.outputWidth(descriptor))
                .height(VulkanDeferral.outputHeight(descriptor))
                .layers(1);
            LongBuffer pointer = stack.mallocLong(1);
            VulkanUtils.crashIfFailure(device, VK12.vkCreateFramebuffer(device.vkDevice(), framebufferInfo, null, pointer), "Failed to create VkFramebuffer");
            long framebuffer = pointer.get(0);
            encoder.queueForDestroy(() -> VK12.vkDestroyFramebuffer(device.vkDevice(), framebuffer, null));
            VkRenderPassBeginInfo beginInfo = VkRenderPassBeginInfo.calloc(stack).sType$Default()
                .renderPass(renderPass)
                .framebuffer(framebuffer)
                .renderArea(renderArea)
                .pClearValues(clearValues.flip());
            VK12.vkCmdBeginRenderPass2(commandBuffer, beginInfo, VkSubpassBeginInfo.calloc(stack).sType$Default().contents(VK12.VK_SUBPASS_CONTENTS_INLINE));
        }
    }

    public static void endRenderPass(VkCommandBuffer commandBuffer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VK12.vkCmdEndRenderPass2(commandBuffer, VkSubpassEndInfo.calloc(stack).sType$Default());
        }
    }

    public static void endRendering(VulkanDevice device, VkCommandBuffer commandBuffer) {
        if (((Device) device).polydium$dynamicRendering()) {
            KHRDynamicRendering.vkCmdEndRenderingKHR(commandBuffer);
        } else {
            endRenderPass(commandBuffer);
        }
    }

    public static final class RenderPassCache {
        private final VulkanDevice device;
        private final Object2LongOpenHashMap<Description> renderPasses = new Object2LongOpenHashMap<>();

        public RenderPassCache(VulkanDevice device) {
            this.device = device;
        }

        // Pipelines: compatibility ignores load ops, so LOAD matches every pass with the same formats
        public long get(VkPipelineRenderingCreateInfoKHR renderingInfo) {
            IntBuffer formats = renderingInfo.pColorAttachmentFormats();
            List<@Nullable Attachment> colors = new ArrayList<>(renderingInfo.colorAttachmentCount());
            for (int i = 0; i < renderingInfo.colorAttachmentCount(); i++) {
                int format = formats.get(i);
                colors.add(format == 0 ? null : new Attachment(format, VK12.VK_ATTACHMENT_LOAD_OP_LOAD));
            }
            int depthFormat = renderingInfo.depthAttachmentFormat();
            return this.get(new Description(colors, depthFormat == 0 ? null : new Attachment(depthFormat, VK12.VK_ATTACHMENT_LOAD_OP_LOAD)));
        }

        public long get(RenderPassDescriptor descriptor) {
            List<@Nullable Attachment> colors = new ArrayList<>(descriptor.colorAttachments().size());
            for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> color : descriptor.colorAttachments()) {
                colors.add(color == null ? null : attachment(color.textureView(), color.clearValue().isPresent()));
            }
            RenderPassDescriptor.Attachment<OptionalDouble> depth = descriptor.depthAttachment();
            return this.get(new Description(colors, depth == null ? null : attachment(depth.textureView(), depth.clearValue().isPresent())));
        }

        private static Attachment attachment(GpuTextureView view, boolean clear) {
            return new Attachment(VulkanConst.toVk(view.texture().getFormat()), clear ? VK12.VK_ATTACHMENT_LOAD_OP_CLEAR : VK12.VK_ATTACHMENT_LOAD_OP_LOAD);
        }

        private synchronized long get(Description description) {
            return this.renderPasses.computeIfAbsent(description, this::create);
        }

        public synchronized void destroy() {
            this.renderPasses.values().forEach(renderPass -> VK12.vkDestroyRenderPass(this.device.vkDevice(), renderPass, null));
            this.renderPasses.clear();
        }

        // Unused color slots reference VK_ATTACHMENT_UNUSED rather than 26.4-snapshot-1's format 0 attachment, which isn't valid Vulkan
        private long create(Description description) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                int colorCount = description.colors().size();
                VkAttachmentDescription2.Buffer attachments = VkAttachmentDescription2.calloc(colorCount + 1, stack);
                VkAttachmentReference2.Buffer colorReferences = VkAttachmentReference2.calloc(colorCount, stack);
                for (Attachment color : description.colors()) {
                    VkAttachmentReference2 reference = colorReferences.get().sType$Default();
                    if (color == null) {
                        reference.attachment(VK12.VK_ATTACHMENT_UNUSED);
                    } else {
                        reference.attachment(attachments.position()).layout(VK12.VK_IMAGE_LAYOUT_GENERAL).aspectMask(VK12.VK_IMAGE_ASPECT_COLOR_BIT);
                        describe(attachments.get(), color);
                    }
                }

                VkSubpassDescription2.Buffer subpass = VkSubpassDescription2.calloc(1, stack).sType$Default().pipelineBindPoint(VK12.VK_PIPELINE_BIND_POINT_GRAPHICS);
                subpass.pColorAttachments(colorCount == 0 ? null : colorReferences.flip());
                subpass.colorAttachmentCount(colorCount);
                if (description.depth() != null) {
                    subpass.pDepthStencilAttachment(VkAttachmentReference2.calloc(stack).sType$Default()
                        .attachment(attachments.position()).layout(VK12.VK_IMAGE_LAYOUT_GENERAL).aspectMask(VK12.VK_IMAGE_ASPECT_DEPTH_BIT));
                    describe(attachments.get(), description.depth());
                }

                VkRenderPassCreateInfo2 createInfo = VkRenderPassCreateInfo2.calloc(stack).sType$Default().pAttachments(attachments.flip()).pSubpasses(subpass);
                LongBuffer pointer = stack.mallocLong(1);
                VulkanUtils.crashIfFailure(this.device, VK12.vkCreateRenderPass2(this.device.vkDevice(), createInfo, null, pointer), "Failed to create VkRenderPass");
                return pointer.get(0);
            }
        }

        // Images stay in GENERAL for their whole life, same as the dynamic rendering path
        private static void describe(VkAttachmentDescription2 attachment, Attachment description) {
            attachment.sType$Default()
                .format(description.format())
                .samples(VK12.VK_SAMPLE_COUNT_1_BIT)
                .loadOp(description.loadOp())
                .storeOp(VK12.VK_ATTACHMENT_STORE_OP_STORE)
                .initialLayout(VK12.VK_IMAGE_LAYOUT_GENERAL)
                .finalLayout(VK12.VK_IMAGE_LAYOUT_GENERAL);
        }

        private record Attachment(int format, int loadOp) {
        }

        // Null colors are unused slots
        private record Description(List<@Nullable Attachment> colors, @Nullable Attachment depth) {
        }
    }

    public static final class DescriptorPools {
        private final VulkanDevice device;
        private final VulkanCommandEncoder encoder;
        private final LongArrayList free = new LongArrayList();
        private long current;

        public DescriptorPools(VulkanDevice device, VulkanCommandEncoder encoder) {
            this.device = device;
            this.encoder = encoder;
        }

        public long allocate(long setLayout) {
            if (this.current != 0L) {
                long set = this.allocateSet(setLayout);
                if (set != 0L) {
                    return set;
                }
                this.retire();
            }

            this.current = this.free.isEmpty() ? this.createPool() : this.free.removeLong(this.free.size() - 1);
            long set = this.allocateSet(setLayout);
            if (set == 0L) {
                throw new IllegalStateException("Failed to allocate descriptor set from new pool");
            }
            return set;
        }

        // Pools are reset once the GPU is done with every set allocated from them
        public void retire() {
            long pool = this.current;
            if (pool == 0L) {
                return;
            }
            this.current = 0L;
            this.encoder.queueForDestroy(() -> {
                VulkanUtils.crashIfFailure(this.device, VK12.vkResetDescriptorPool(this.device.vkDevice(), pool, 0), "Failed to reset descriptor pool");
                this.free.add(pool);
            });
        }

        public void destroy() {
            if (this.current != 0L) {
                this.free.add(this.current);
                this.current = 0L;
            }
            this.free.forEach(pool -> VK12.vkDestroyDescriptorPool(this.device.vkDevice(), pool, null));
            this.free.clear();
        }

        private long createPool() {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorPoolSize.Buffer sizes = VkDescriptorPoolSize.calloc(3, stack);
                sizes.get(0).type(VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER).descriptorCount(4096);
                sizes.get(1).type(VK12.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(4096);
                sizes.get(2).type(VK12.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER).descriptorCount(128);
                VkDescriptorPoolCreateInfo createInfo = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(1024).pPoolSizes(sizes);
                LongBuffer pointer = stack.mallocLong(1);
                VulkanUtils.crashIfFailure(this.device, VK12.vkCreateDescriptorPool(this.device.vkDevice(), createInfo, null, pointer), "Failed to allocate descriptor pool");
                return pointer.get(0);
            }
        }

        // 0 when the pool is exhausted or fragmented
        private long allocateSet(long setLayout) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default()
                    .descriptorPool(this.current)
                    .pSetLayouts(stack.longs(setLayout));
                LongBuffer pointer = stack.mallocLong(1);
                int result = VK12.vkAllocateDescriptorSets(this.device.vkDevice(), allocateInfo, pointer);
                if (result == VK12.VK_ERROR_OUT_OF_POOL_MEMORY || result == VK12.VK_ERROR_FRAGMENTED_POOL) {
                    return 0L;
                }
                VulkanUtils.crashIfFailure(this.device, result, "Failed to allocate descriptor set");
                return pointer.get(0);
            }
        }
    }
}
//?}
