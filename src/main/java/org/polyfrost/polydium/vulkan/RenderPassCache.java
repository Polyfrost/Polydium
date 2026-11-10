package org.polyfrost.polydium.vulkan;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import com.mojang.blaze3d.textures.GpuTexture;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkAttachmentDescription2;
import org.lwjgl.vulkan.VkAttachmentReference2;
import org.lwjgl.vulkan.VkRenderPassCreateInfo2;
import org.lwjgl.vulkan.VkSubpassDescription2;
import org.polyfrost.polydium.vulkan.compat.RenderPassDescriptor;

// Ported from 26.4-snapshot-1. Only used when VK_KHR_dynamic_rendering is unavailable
public class RenderPassCache implements Destroyable {
  private static final int LOAD_OP_LOAD = 0;
  private static final int LOAD_OP_CLEAR = 1;
  private static final int LOAD_OP_DONT_CARE = 2;
  private final VulkanDevice device;
  private final Object2LongOpenHashMap<RenderPassCache.RenderPassDescription> renderPasses = new Object2LongOpenHashMap<>();

  public RenderPassCache(final VulkanDevice device) {
    this.device = device;
  }

  @Override
  public synchronized void destroy() {
    this.renderPasses.values().forEach(renderPass -> VK12.vkDestroyRenderPass(this.device.vkDevice(), renderPass, null));
    this.renderPasses.clear();
  }

  private long createRenderPass(final RenderPassCache.RenderPassDescription description) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      int colorCount = description.colorAttachments.size();
      VkAttachmentDescription2.Buffer attachments = VkAttachmentDescription2.calloc(colorCount + 1, stack);
      VkAttachmentReference2.Buffer colorReferences = VkAttachmentReference2.calloc(colorCount, stack);

      for (int i = 0; i < colorCount; i++) {
        describe(attachments.get().sType$Default(), description.colorAttachments.get(i));
        colorReferences.get().sType$Default().attachment(i).layout(1).aspectMask(1);
      }

      VkSubpassDescription2.Buffer subpass = VkSubpassDescription2.calloc(1, stack).sType$Default();
      subpass.pipelineBindPoint(0);
      subpass.viewMask(0);
      subpass.pColorAttachments(colorCount == 0 ? null : colorReferences.flip());
      subpass.colorAttachmentCount(colorCount);
      if (description.depthAttachment != null) {
        describe(attachments.get().sType$Default(), description.depthAttachment);
        subpass.pDepthStencilAttachment(VkAttachmentReference2.calloc(stack).sType$Default().attachment(colorCount).layout(1).aspectMask(2));
      }

      VkRenderPassCreateInfo2 createInfo = VkRenderPassCreateInfo2.calloc(stack).sType$Default().pAttachments(attachments.flip()).pSubpasses(subpass);
      LongBuffer pointer = stack.callocLong(1);
      VulkanUtils.crashIfFailure(this.device, VK12.vkCreateRenderPass2(this.device.vkDevice(), createInfo, null, pointer), "Failed to create VkRenderPass");
      return pointer.get(0);
    }
  }

  private static void describe(final VkAttachmentDescription2 attachment, final RenderPassCache.AttachmentDescription description) {
    // Images live in GENERAL for their whole life, same as the dynamic rendering path
    attachment.format(description.format).samples(1).loadOp(description.loadOp).initialLayout(1).finalLayout(1);
    if (description.loadOp == LOAD_OP_DONT_CARE) {
      // Unused slots share one placeholder image, see VulkanCommandEncoder#unusedAttachment
      attachment.flags(1).storeOp(1);
    } else {
      attachment.storeOp(0);
    }
  }

  private synchronized long getRenderPass(final RenderPassCache.RenderPassDescription description) {
    return this.renderPasses.computeIfAbsent(description, this::createRenderPass);
  }

  // Pipelines: compatibility ignores load/store ops, so LOAD matches every pass with the same formats
  public long getRenderPass(final int depthFormat, final int[] colorFormats) {
    List<RenderPassCache.AttachmentDescription> colorAttachments = new ArrayList<>(colorFormats.length);

    for (int colorFormat : colorFormats) {
      colorAttachments.add(new RenderPassCache.AttachmentDescription(colorFormat, LOAD_OP_LOAD));
    }

    return this.getRenderPass(
      new RenderPassCache.RenderPassDescription(colorAttachments, depthFormat == 0 ? null : new RenderPassCache.AttachmentDescription(depthFormat, LOAD_OP_LOAD))
    );
  }

  // Passes: unused color slots take unusedFormat, since a backport pipeline gives every color target the same format
  public long getRenderPass(final RenderPassDescriptor descriptor, final int unusedFormat) {
    List<RenderPassCache.AttachmentDescription> colorAttachments = new ArrayList<>(descriptor.colorAttachments().size());

    for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> colorAttachment : descriptor.colorAttachments()) {
      if (colorAttachment == null) {
        colorAttachments.add(new RenderPassCache.AttachmentDescription(unusedFormat, LOAD_OP_DONT_CARE));
      } else {
        colorAttachments.add(
          new RenderPassCache.AttachmentDescription(format(colorAttachment.textureView().texture()), colorAttachment.clearValue().isPresent() ? LOAD_OP_CLEAR : LOAD_OP_LOAD)
        );
      }
    }

    RenderPassDescriptor.Attachment<OptionalDouble> depth = descriptor.depthAttachment();
    RenderPassCache.AttachmentDescription depthAttachment = depth == null
      ? null
      : new RenderPassCache.AttachmentDescription(format(depth.textureView().texture()), depth.clearValue().isPresent() ? LOAD_OP_CLEAR : LOAD_OP_LOAD);
    return this.getRenderPass(new RenderPassCache.RenderPassDescription(colorAttachments, depthAttachment));
  }

  public static int format(final GpuTexture texture) {
    return VulkanConst.toVk(((VulkanGpuTexture)texture).gpuFormat());
  }

  private record AttachmentDescription(int format, int loadOp) {
  }

  private record RenderPassDescription(List<RenderPassCache.AttachmentDescription> colorAttachments, RenderPassCache.@Nullable AttachmentDescription depthAttachment) {
  }
}
