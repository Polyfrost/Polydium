package org.polyfrost.polydium.vulkan;

import java.util.function.Supplier;
import java.util.OptionalInt;
import org.polyfrost.polydium.vulkan.compat.RenderArea;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.systems.CommandEncoderBackend;
import org.polyfrost.polydium.vulkan.compat.GpuQueryPool;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import org.polyfrost.polydium.vulkan.compat.RenderPassDescriptor;
import org.polyfrost.polydium.vulkan.compat.TransientMemory;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.polyfrost.polydium.vulkan.checkpoints.CheckpointExtension;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearDepthStencilValue;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageCopy;
import org.lwjgl.vulkan.VkImageSubresourceLayers;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkMemoryBarrier2;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderingAttachmentInfo;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreTypeCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreWaitInfo;
import org.lwjgl.vulkan.VkSubpassBeginInfo;
import org.lwjgl.vulkan.VkSubpassEndInfo;
import org.lwjgl.vulkan.VkMemoryBarrier2.Buffer;

public class VulkanCommandEncoder implements CommandEncoderBackend, Destroyable {
  public static final int MAX_SUBMITS_IN_FLIGHT = 2;
  private final VulkanDevice device;
  private final VulkanTransientMemory transientMemory;
  private final long submitSemaphore;
  private long currentSubmitIndex = 2L;
  private long completedSubmitIndex = 0L;
  private final CheckpointExtension.CheckpointStorage checkpointStorage;
  private VulkanQueue.Submission submissionBuilder;
  private final DestructionQueue<Destroyable> destroyQueue = new DestructionQueue<>(2, Destroyable::destroy);
  private final VulkanCommandPool[] commandPools = new VulkanCommandPool[2];
  private @Nullable VkCommandBuffer currentInitCommandBuffer;
  private @Nullable VkCommandBuffer currentCommandBuffer;
  private @Nullable VulkanRenderPass currentRenderPass;
  private final List<DescriptorPool> freeDescriptorPools = new ArrayList<>();
  private @Nullable DescriptorPool currentDescriptorPool;
  private @Nullable GpuTexture unusedTexture;
  private @Nullable GpuTextureView unusedView;

  public VulkanCommandEncoder(final VulkanDevice device) {
    this.device = device;
    this.transientMemory = new VulkanTransientMemory(device, this);
    MemoryStack baseStack = MemoryStack.stackGet();
    MemoryStack stack = baseStack.push();

    try {
      VkSemaphoreTypeCreateInfo semaphoreTypeCreateInfo = VkSemaphoreTypeCreateInfo.calloc(stack).sType$Default();
      semaphoreTypeCreateInfo.semaphoreType(1);
      semaphoreTypeCreateInfo.initialValue(this.currentSubmitIndex - 1L);
      VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack).sType$Default();
      semaphoreCreateInfo.pNext(semaphoreTypeCreateInfo);
      LongBuffer semaphoreHandlePtr = stack.callocLong(1);
      VulkanUtils.crashIfFailure(
        device, VK12.vkCreateSemaphore(device.vkDevice(), semaphoreCreateInfo, null, semaphoreHandlePtr), "Failed to create submit VkSemaphore"
      );
      this.submitSemaphore = semaphoreHandlePtr.get(0);
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

    for (int i = 0; i < 2; i++) {
      this.commandPools[i] = new VulkanCommandPool(device, device.graphicsQueue());
    }

    this.checkpointStorage = device.checkpointExtension().createStorage(device, device.graphicsQueue(), 2);
    this.submissionBuilder = device.graphicsQueue().beginSubmit();
    this.transientMemory.beginSubmit();
  }

  @Override
  public void destroy() {
    this.releaseUnusedAttachment();
    this.transientMemory.endSubmit();
    this.submissionBuilder.close();
    this.device.graphicsQueue().waitIdle();
    this.destroyQueue.close();
    this.transientMemory.destroy();
    this.destroyQueue.close();

    for (int i = 0; i < 2; i++) {
      this.commandPools[i].destroy();
    }

    if (this.currentDescriptorPool != null) {
      this.currentDescriptorPool.destroy();
    }

    this.freeDescriptorPools.forEach(DescriptorPool::destroy);
    VK12.vkDestroySemaphore(this.device.vkDevice(), this.submitSemaphore, null);
  }

  public void queueForDestroy(final Destroyable destroyable) {
    this.destroyQueue.add(destroyable);
  }

  private VulkanCommandPool currentCommandPool() {
    return this.commandPools[(int)(this.currentSubmitIndex % 2L)];
  }

  public VkCommandBuffer allocateAndBeginTransientCommandBuffer() {
    MemoryStack stack = MemoryStack.stackPush();

    VkCommandBuffer var4;
    try {
      VkCommandBuffer commandBuffer = this.currentCommandPool().allocateBuffer();
      VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack).sType$Default();
      beginInfo.flags(1);
      VulkanUtils.crashIfFailure(this.device, VK12.vkBeginCommandBuffer(commandBuffer, beginInfo), "Failed to begin VkCommandBuffer");
      var4 = commandBuffer;
    } catch (Throwable var6) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var5) {
          var6.addSuppressed(var5);
        }
      }

      throw var6;
    }

    if (stack != null) {
      stack.close();
    }

    return var4;
  }

  public VkCommandBuffer commandBuffer() {
    if (this.currentCommandBuffer != null) {
      return this.currentCommandBuffer;
    } else if (this.currentRenderPass != null) {
      throw new IllegalStateException("Cannot start command buffer while inside RenderPass");
    } else {
      this.currentCommandBuffer = this.allocateAndBeginTransientCommandBuffer();
      return this.currentCommandBuffer;
    }
  }

  VkCommandBuffer objectInitCommandBuffer() {
    if (this.currentInitCommandBuffer != null) {
      return this.currentInitCommandBuffer;
    } else {
      this.currentInitCommandBuffer = this.allocateAndBeginTransientCommandBuffer();
      return this.currentInitCommandBuffer;
    }
  }

  private void endCommandBuffer() {
    if (this.currentCommandBuffer != null || this.currentInitCommandBuffer != null) {
      if (this.currentRenderPass != null) {
        throw new IllegalStateException("Cannot end command buffer while inside RenderPass");
      } else {
        if (this.currentInitCommandBuffer != null) {
          try (MemoryStack stack = MemoryStack.stackPush()) {
            memoryBarrier(this.currentInitCommandBuffer, stack);
          }

          VulkanUtils.crashIfFailure(this.device, VK12.vkEndCommandBuffer(this.currentInitCommandBuffer), "Failed to end VkCommandBuffer");
          this.submissionBuilder.executeCommands(this.currentInitCommandBuffer);
          this.currentInitCommandBuffer = null;
        }

        if (this.currentCommandBuffer != null) {
          VulkanUtils.crashIfFailure(this.device, VK12.vkEndCommandBuffer(this.currentCommandBuffer), "Failed to end VkCommandBuffer");
          this.submissionBuilder.executeCommands(this.currentCommandBuffer);
          this.currentCommandBuffer = null;
        }
      }
    }
  }

  public void waitSemaphore(final long vkSemaphore, final long value, final long stageMask) {
    if (this.currentRenderPass != null) {
      throw new IllegalStateException("Cannot add semaphore operation while inside RenderPass");
    } else {
      this.endCommandBuffer();
      this.submissionBuilder.waitSemaphore(vkSemaphore, value, stageMask);
    }
  }

  public void execute(final VkCommandBuffer commandBuffer) {
    if (this.currentRenderPass != null) {
      throw new IllegalStateException("Cannot execute command buffer while inside RenderPass");
    } else {
      this.endCommandBuffer();
      this.submissionBuilder.executeCommands(commandBuffer);
    }
  }

  public void signalSemaphore(final long vkSemaphore, final long value, final long stageMask) {
    if (this.currentRenderPass != null) {
      throw new IllegalStateException("Cannot add semaphore operation while inside RenderPass");
    } else {
      this.endCommandBuffer();
      this.submissionBuilder.signalSemaphore(vkSemaphore, value, stageMask);
    }
  }

  private void memoryBarrier(final MemoryStack stack) {
    memoryBarrier(this.commandBuffer(), stack);
  }

  public static void memoryBarrier(final VkCommandBuffer commandBuffer, final MemoryStack stack) {
    Buffer memoryBarrier = VkMemoryBarrier2.calloc(1, stack).sType$Default();
    memoryBarrier.srcStageMask(65536L);
    memoryBarrier.srcAccessMask(98304L);
    memoryBarrier.dstStageMask(65536L);
    memoryBarrier.dstAccessMask(98304L);
    VkDependencyInfo depInfo = VkDependencyInfo.calloc(stack).sType$Default();
    depInfo.pMemoryBarriers(memoryBarrier);
    KHRSynchronization2.vkCmdPipelineBarrier2KHR(commandBuffer, depInfo);
  }

  public long currentSubmitIndex() {
    return this.currentSubmitIndex;
  }

  public void submit() {
    this.endCommandBuffer();
    this.transientMemory.endSubmit();
    this.signalSemaphore(this.submitSemaphore, this.currentSubmitIndex, 65536L);
    if (this.currentDescriptorPool != null) {
      this.retireDescriptorPool(this.currentDescriptorPool);
      this.currentDescriptorPool = null;
    }

    this.submissionBuilder.close();
    this.submissionBuilder = this.device.graphicsQueue().beginSubmit();
    this.currentSubmitIndex++;
    if (!this.awaitSubmitCompletion(this.currentSubmitIndex - 2L, 5000000000L)) {
      List<CheckpointExtension.QueueCheckpoints> checkpoints = this.device.checkpointExtension().retrieveCheckpoints(false);
      throw new IllegalStateException("5s timeout reached when waiting for VK semaphore: " + VulkanUtils.formatCheckpoints(checkpoints));
    } else {
      this.currentCommandPool().reset();
      this.destroyQueue.rotate();
      this.checkpointStorage.rotate();
      this.transientMemory.beginSubmit();
    }
  }

  private void retireDescriptorPool(final DescriptorPool pool) {
    this.queueForDestroy(() -> {
      pool.reset();
      this.freeDescriptorPools.add(pool);
    });
  }

  long allocateDescriptorSet(final long setLayout) {
    if (this.currentDescriptorPool != null) {
      long set = this.currentDescriptorPool.allocateSet(setLayout);
      if (set != 0L) {
        return set;
      }

      this.retireDescriptorPool(this.currentDescriptorPool);
    }

    this.currentDescriptorPool = this.freeDescriptorPools.isEmpty()
      ? new DescriptorPool(this.device)
      : this.freeDescriptorPools.remove(this.freeDescriptorPools.size() - 1);
    long set = this.currentDescriptorPool.allocateSet(setLayout);
    if (set == 0L) {
      throw new IllegalStateException("Failed to allocate descriptor set from new pool");
    }

    return set;
  }

  public TransientMemory transientMemory() {
    return this.transientMemory;
  }

  public RenderPassBackend createRenderPass(final RenderPassDescriptor descriptor) {
    List<RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colorAttachments = descriptor.colorAttachments();
    VulkanGpuTextureView[] colorTextures = new VulkanGpuTextureView[colorAttachments.size()];

    for (int i = 0; i < colorAttachments.size(); i++) {
      RenderPassDescriptor.Attachment<Optional<Vector4fc>> attachment = colorAttachments.get(i);
      colorTextures[i] = attachment != null ? (VulkanGpuTextureView)attachment.textureView() : null;
    }

    RenderPassDescriptor.Attachment<OptionalDouble> depthAttachment = descriptor.depthAttachment();
    this.device.instance().debug().beginDebugGroup(this.commandBuffer(), descriptor.label());
    this.checkpointStorage.recordCheckpoint(this.commandBuffer(), CheckpointExtension.CheckpointType.BEGIN_RENDER_PASS, descriptor.label());
    MemoryStack stack = MemoryStack.stackPush();

    try {
      int width = 0;
      int height = 0;
      if (!colorAttachments.isEmpty()) {
        for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> colorAttachment : colorAttachments) {
          if (colorAttachment != null) {
            GpuTextureView colorTexture = colorAttachment.textureView();
            width = colorTexture.getWidth(0);
            height = colorTexture.getHeight(0);
          }
        }
      } else if (depthAttachment != null) {
        width = depthAttachment.textureView().getWidth(0);
        height = depthAttachment.textureView().getHeight(0);
      }

      VkRect2D vkRenderArea = VkRect2D.calloc(stack);

      assert descriptor.renderArea != null;

      vkRenderArea.extent().set(descriptor.renderArea.width(), descriptor.renderArea.height());
      vkRenderArea.offset().set(descriptor.renderArea.x(), descriptor.renderArea.y());
      if (!this.device.dynamicRendering()) {
        this.beginLegacyRenderPass(stack, descriptor, colorTextures, depthAttachment, vkRenderArea, width, height);
      } else {
        org.lwjgl.vulkan.VkRenderingAttachmentInfo.Buffer colorAttachmentInfo = VkRenderingAttachmentInfo.calloc(colorAttachments.size(), stack);

        for (int i = 0; i < colorAttachments.size(); i++) {
          ((org.lwjgl.vulkan.VkRenderingAttachmentInfo.Buffer)colorAttachmentInfo.position(i)).sType$Default();
          VulkanGpuTextureView colorTexture = colorTextures[i];
          if (colorTexture != null) {
            colorAttachmentInfo.imageView(colorTexture.vkImageView());
            colorAttachmentInfo.imageLayout(1);
            colorAttachmentInfo.storeOp(0);
            RenderPassDescriptor.Attachment<Optional<Vector4fc>> attachment = colorAttachments.get(i);
            Optional<Vector4fc> clearValue = attachment.clearValue();
            if (clearValue.isPresent()) {
              Vector4fc color = clearValue.get();
              VkClearColorValue vkClearColor = VulkanUtils.putArgb(VkClearColorValue.calloc(stack), color);
              colorAttachmentInfo.loadOp(1);
              colorAttachmentInfo.clearValue(VkClearValue.calloc(stack).color(vkClearColor));
            } else {
              colorAttachmentInfo.loadOp(0);
            }
          } else {
            colorAttachmentInfo.imageView(0L);
            colorAttachmentInfo.imageLayout(0);
            colorAttachmentInfo.storeOp(1);
            colorAttachmentInfo.loadOp(2);
          }
        }

        colorAttachmentInfo.position(0);
        VkRenderingInfo renderingInfo = VkRenderingInfo.calloc(stack).sType$Default();
        renderingInfo.renderArea(vkRenderArea);
        renderingInfo.layerCount(1);
        renderingInfo.viewMask(0);
        renderingInfo.pColorAttachments(colorAttachmentInfo);
        if (depthAttachment != null) {
          VkRenderingAttachmentInfo depthAttachmentInfo = VkRenderingAttachmentInfo.calloc(stack).sType$Default();
          VulkanGpuTextureView vulkanDepthAttachment = (VulkanGpuTextureView)depthAttachment.textureView();
          depthAttachmentInfo.imageView(vulkanDepthAttachment.vkImageView());
          depthAttachmentInfo.imageLayout(1);
          depthAttachmentInfo.storeOp(0);
          OptionalDouble clearValue = depthAttachment.clearValue();
          if (clearValue.isPresent()) {
            double color = clearValue.getAsDouble();
            VkClearDepthStencilValue vkClearColor = VkClearDepthStencilValue.calloc(stack).depth((float)color);
            depthAttachmentInfo.loadOp(1);
            depthAttachmentInfo.clearValue(VkClearValue.calloc(stack).depthStencil(vkClearColor));
          } else {
            depthAttachmentInfo.loadOp(0);
          }

          renderingInfo.pDepthAttachment(depthAttachmentInfo);
        }

        KHRDynamicRendering.vkCmdBeginRenderingKHR(this.commandBuffer(), renderingInfo);
      }

      this.currentRenderPass = new VulkanRenderPass(
        this.device, this, this.commandBuffer(), this.checkpointStorage, descriptor.renderArea, width, height, depthAttachment != null, descriptor.label()
      );
    } catch (Throwable var18) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var17) {
          var18.addSuppressed(var17);
        }
      }

      throw var18;
    }

    if (stack != null) {
      stack.close();
    }

    return this.currentRenderPass;
  }

  public void submitRenderPass() {
    if (this.currentRenderPass == null) {
      throw new IllegalStateException("Cannot submit a renderpass if one hasn't been started!");
    } else {
      endRendering(this.device, this.commandBuffer());
      this.device.instance().debug().endDebugGroup(this.commandBuffer());
      this.checkpointStorage.recordCheckpoint(this.commandBuffer(), CheckpointExtension.CheckpointType.END_RENDER_PASS, this.currentRenderPass.getLabel());
      this.currentRenderPass = null;
      MemoryStack stack = MemoryStack.stackPush();

      try {
        this.memoryBarrier(stack);
      } catch (Throwable var5) {
        if (stack != null) {
          try {
            stack.close();
          } catch (Throwable var4) {
            var5.addSuppressed(var4);
          }
        }

        throw var5;
      }

      if (stack != null) {
        stack.close();
      }
    }
  }

  // 26.4-snapshot-1's path for devices without VK_KHR_dynamic_rendering
  private void beginLegacyRenderPass(
    final MemoryStack stack,
    final RenderPassDescriptor descriptor,
    final @Nullable VulkanGpuTextureView[] colorTextures,
    final RenderPassDescriptor.@Nullable Attachment<OptionalDouble> depthAttachment,
    final VkRect2D renderArea,
    final int width,
    final int height
  ) {
    GpuTexture firstColor = null;
    for (VulkanGpuTextureView colorTexture : colorTextures) {
      if (colorTexture != null) {
        firstColor = colorTexture.texture();
        break;
      }
    }

    LongBuffer attachments = stack.callocLong(colorTextures.length + 1);
    VkClearValue.Buffer clearValues = VkClearValue.calloc(colorTextures.length + 1, stack);
    List<RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colorAttachments = descriptor.colorAttachments();

    for (int i = 0; i < colorTextures.length; i++) {
      VkClearValue clearValue = clearValues.get();
      if (colorTextures[i] == null) {
        // Framebuffers can't hold a null view, so unused slots write to a throwaway image
        attachments.put(this.unusedAttachment(Objects.requireNonNull(firstColor, "Render pass has no used color attachment"), width, height));
      } else {
        attachments.put(colorTextures[i].vkImageView());
        colorAttachments.get(i).clearValue().ifPresent(color -> VulkanUtils.putArgb(clearValue.color(), color));
      }
    }

    if (depthAttachment != null) {
      attachments.put(((VulkanGpuTextureView)depthAttachment.textureView()).vkImageView());
      VkClearValue clearValue = clearValues.get();
      depthAttachment.clearValue().ifPresent(depth -> clearValue.depthStencil().depth((float)depth));
    }

    attachments.flip();
    clearValues.flip();
    long renderPass = this.device.renderPassCache().getRenderPass(descriptor, firstColor == null ? 0 : RenderPassCache.format(firstColor));
    VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack).sType$Default()
      .renderPass(renderPass)
      .pAttachments(attachments)
      .width(width)
      .height(height)
      .layers(1);
    LongBuffer pointer = stack.callocLong(1);
    VulkanUtils.crashIfFailure(this.device, VK12.vkCreateFramebuffer(this.device.vkDevice(), framebufferInfo, null, pointer), "Failed to create VkFramebuffer");
    long framebuffer = pointer.get(0);
    this.queueForDestroy(() -> VK12.vkDestroyFramebuffer(this.device.vkDevice(), framebuffer, null));
    VkRenderPassBeginInfo beginInfo = VkRenderPassBeginInfo.calloc(stack).sType$Default()
      .renderPass(renderPass)
      .framebuffer(framebuffer)
      .renderArea(renderArea)
      .pClearValues(clearValues);
    VK12.vkCmdBeginRenderPass2(this.commandBuffer(), beginInfo, VkSubpassBeginInfo.calloc(stack).sType$Default().contents(0));
  }

  private long unusedAttachment(final GpuTexture like, final int width, final int height) {
    GpuTexture texture = this.unusedTexture;
    if (texture == null || texture.getFormat() != like.getFormat() || texture.getWidth(0) != width || texture.getHeight(0) != height) {
      this.releaseUnusedAttachment();
      texture = this.device.createTexture("Unused color attachment", GpuTexture.USAGE_RENDER_ATTACHMENT, like.getFormat(), width, height, 1, 1);
      this.unusedTexture = texture;
      this.unusedView = this.device.createTextureView(texture);
    }

    return ((VulkanGpuTextureView)Objects.requireNonNull(this.unusedView)).vkImageView();
  }

  private void releaseUnusedAttachment() {
    if (this.unusedView != null) {
      this.unusedView.close();
      this.unusedView = null;
    }

    if (this.unusedTexture != null) {
      this.unusedTexture.close();
      this.unusedTexture = null;
    }
  }

  public static void endRendering(final VulkanDevice device, final VkCommandBuffer commandBuffer) {
    if (device.dynamicRendering()) {
      KHRDynamicRendering.vkCmdEndRenderingKHR(commandBuffer);
    } else {
      try (MemoryStack stack = MemoryStack.stackPush()) {
        VK12.vkCmdEndRenderPass2(commandBuffer, VkSubpassEndInfo.calloc(stack).sType$Default());
      }
    }
  }

  private void clearColorTextureUnsynced(final MemoryStack stack, final GpuTexture colorTexture, final Vector4fc clearColor) {
    VkClearColorValue vkClearColor = VulkanUtils.putArgb(VkClearColorValue.calloc(stack), clearColor);
    VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack);
    subresourceRange.baseMipLevel(0);
    subresourceRange.levelCount(colorTexture.getMipLevels());
    subresourceRange.baseArrayLayer(0);
    subresourceRange.layerCount(1);
    subresourceRange.aspectMask(1);
    VK12.vkCmdClearColorImage(this.commandBuffer(), ((VulkanGpuTexture)colorTexture).vkImage(), 1, vkClearColor, subresourceRange);
  }

  public void clearDepthTextureUnsynced(final MemoryStack stack, final GpuTexture depthTexture, final double clearDepth) {
    VkClearDepthStencilValue vkClearDepth = VkClearDepthStencilValue.calloc(stack).depth((float)clearDepth);
    VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack);
    subresourceRange.baseMipLevel(0);
    subresourceRange.levelCount(depthTexture.getMipLevels());
    subresourceRange.baseArrayLayer(0);
    subresourceRange.layerCount(1);
    subresourceRange.aspectMask(2);
    VK12.vkCmdClearDepthStencilImage(this.commandBuffer(), ((VulkanGpuTexture)depthTexture).vkImage(), 1, vkClearDepth, subresourceRange);
  }

  public void clearColorTexture(final GpuTexture colorTexture, final Vector4fc clearColor) {
    MemoryStack stack = MemoryStack.stackPush();

    try {
      this.clearColorTextureUnsynced(stack, colorTexture, clearColor);
      this.memoryBarrier(stack);
    } catch (Throwable var7) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var6) {
          var7.addSuppressed(var6);
        }
      }

      throw var7;
    }

    if (stack != null) {
      stack.close();
    }
  }

  public void clearColorAndDepthTextures(final GpuTexture colorTexture, final Vector4fc clearColor, final GpuTexture depthTexture, final double clearDepth) {
    MemoryStack stack = MemoryStack.stackPush();

    try {
      this.clearColorTextureUnsynced(stack, colorTexture, clearColor);
      this.clearDepthTextureUnsynced(stack, depthTexture, clearDepth);
      this.memoryBarrier(stack);
    } catch (Throwable var10) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var9) {
          var10.addSuppressed(var9);
        }
      }

      throw var10;
    }

    if (stack != null) {
      stack.close();
    }
  }

  public void clearColorAndDepthTextures(
    final GpuTexture colorTexture,
    final Vector4fc clearColor,
    final GpuTexture depthTexture,
    final double clearDepth,
    final int regionX,
    final int regionY,
    final int regionWidth,
    final int regionHeight
  ) {
    try (
      GpuTextureView colorTextureView = this.device.createTextureView(colorTexture, 0, 1);
      GpuTextureView depthTextureView = this.device.createTextureView(depthTexture, 0, 1);
    ) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
        this.createRenderPass(
          RenderPassDescriptor.create(() -> "ClearColorDepthTextures")
            .withColorAttachment(colorTextureView)
            .withDepthAttachment(depthTextureView)
            .withRenderArea(new RenderArea(0, 0, colorTexture.getWidth(0), colorTexture.getHeight(0)))
        );

        assert this.currentRenderPass != null;

        org.lwjgl.vulkan.VkClearRect.Buffer rects = VkClearRect.calloc(1, stack);
        rects.baseArrayLayer(0);
        rects.layerCount(1);
        rects.rect().offset().set(regionX, regionY);
        rects.rect().extent().set(regionWidth, regionHeight);
        org.lwjgl.vulkan.VkClearAttachment.Buffer attachments = VkClearAttachment.calloc(2, stack);
        VkClearValue colorClearValue = VkClearValue.calloc(stack);
        VulkanUtils.putArgb(colorClearValue.color(), clearColor);
        attachments.aspectMask(1);
        attachments.clearValue(colorClearValue);
        VkClearValue depthClearValue = VkClearValue.calloc(stack);
        VkClearDepthStencilValue clearValue = depthClearValue.depthStencil();
        clearValue.depth((float)clearDepth);
        attachments.position(1);
        attachments.aspectMask(2);
        attachments.clearValue(depthClearValue);
        attachments.position(0);
        VK12.vkCmdClearAttachments(this.commandBuffer(), attachments, rects);
        this.submitRenderPass();
      } catch (Throwable var21) {
        if (stack != null) {
          try {
            stack.close();
          } catch (Throwable var20) {
            var21.addSuppressed(var20);
          }
        }

        throw var21;
      }

      if (stack != null) {
        stack.close();
      }
    }
  }

  @Override
  public void clearDepthTexture(final GpuTexture depthTexture, final double clearDepth) {
    MemoryStack stack = MemoryStack.stackPush();

    try {
      this.clearDepthTextureUnsynced(stack, depthTexture, clearDepth);
      this.memoryBarrier(stack);
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
  }

  @Override
  public void writeToBuffer(final GpuBufferSlice destination, final ByteBuffer data) {
    VulkanGpuBuffer destBuffer = (VulkanGpuBuffer)destination.buffer();
    GpuBufferSlice stagingBuffer = this.transientMemory.uploadStaging(data, 1L, 16);
    MemoryStack stack = MemoryStack.stackPush();

    try {
      org.lwjgl.vulkan.VkBufferCopy.Buffer regions = VkBufferCopy.calloc(1, stack)
        .srcOffset(stagingBuffer.offset())
        .dstOffset(destination.offset())
        .size(data.remaining());
      VK12.vkCmdCopyBuffer(this.commandBuffer(), ((VulkanGpuBuffer)stagingBuffer.buffer()).vkBuffer(), destBuffer.vkBuffer(), regions);
      this.memoryBarrier(stack);
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

  @Override
  public void copyToBuffer(final GpuBufferSlice source, final GpuBufferSlice target) {
    MemoryStack stack = MemoryStack.stackPush();

    try {
      org.lwjgl.vulkan.VkBufferCopy.Buffer copyInfo = VkBufferCopy.calloc(1, stack);
      copyInfo.srcOffset(source.offset());
      copyInfo.dstOffset(target.offset());
      copyInfo.size(source.length());
      VK12.vkCmdCopyBuffer(this.commandBuffer(), ((VulkanGpuBuffer)source.buffer()).vkBuffer(), ((VulkanGpuBuffer)target.buffer()).vkBuffer(), copyInfo);
      this.memoryBarrier(stack);
    } catch (Throwable var7) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var6) {
          var7.addSuppressed(var6);
        }
      }

      throw var7;
    }

    if (stack != null) {
      stack.close();
    }
  }

  public void writeToTexture(
    final GpuTexture destination,
    final ByteBuffer source,
    final int mipLevel,
    final int depthOrLayer,
    final int destX,
    final int destY,
    final int width,
    final int height
  ) {
    GpuBufferSlice stagingBuffer = this.transientMemory.uploadStaging(source, 1L, 16);
    MemoryStack stack = MemoryStack.stackPush();

    try {
      org.lwjgl.vulkan.VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);
      region.bufferOffset(stagingBuffer.offset());
      region.bufferRowLength(width);
      region.bufferImageHeight(height);
      VkImageSubresourceLayers imageSubresource = region.imageSubresource();
      imageSubresource.aspectMask(1);
      imageSubresource.mipLevel(mipLevel);
      imageSubresource.baseArrayLayer(depthOrLayer);
      imageSubresource.layerCount(1);
      region.imageOffset().set(destX, destY, 0);
      region.imageExtent().set(width, height, 1);
      VK12.vkCmdCopyBufferToImage(
        this.commandBuffer(), ((VulkanGpuBuffer)stagingBuffer.buffer()).vkBuffer(), ((VulkanGpuTexture)destination).vkImage(), 1, region
      );
      this.memoryBarrier(stack);
    } catch (Throwable var14) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var13) {
          var14.addSuppressed(var13);
        }
      }

      throw var14;
    }

    if (stack != null) {
      stack.close();
    }
  }

  public void copyBufferToTexture(
    final GpuBufferSlice source,
    final int sourceX,
    final int sourceY,
    final int sourceWidth,
    final int sourceHeight,
    final GpuTexture destination,
    final int destinationX,
    final int destinationY,
    final int copyWidth,
    final int copyHeight,
    final int mipLevel,
    final int arrayLayer
  ) {
    int texelSize = ((VulkanGpuTexture)destination).gpuFormat().blockSize();
    long skipTexels = sourceX + (long)sourceY * sourceWidth;
    long skipBytes = skipTexels * texelSize;
    MemoryStack stack = MemoryStack.stackPush();

    try {
      org.lwjgl.vulkan.VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);
      region.bufferOffset(source.offset() + skipBytes);
      region.bufferRowLength(sourceWidth);
      region.bufferImageHeight(sourceHeight);
      VkImageSubresourceLayers imageSubresource = region.imageSubresource();
      imageSubresource.aspectMask(1);
      imageSubresource.mipLevel(mipLevel);
      imageSubresource.baseArrayLayer(arrayLayer);
      imageSubresource.layerCount(1);
      region.imageOffset().set(destinationX, destinationY, 0);
      region.imageExtent().set(copyWidth, copyHeight, 1);
      VK12.vkCmdCopyBufferToImage(this.commandBuffer(), ((VulkanGpuBuffer)source.buffer()).vkBuffer(), ((VulkanGpuTexture)destination).vkImage(), 1, region);
      this.memoryBarrier(stack);
    } catch (Throwable var22) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var21) {
          var22.addSuppressed(var21);
        }
      }

      throw var22;
    }

    if (stack != null) {
      stack.close();
    }
  }

  @Override
  public void copyTextureToBuffer(final GpuTexture source, final GpuBuffer destination, final long offset, final Runnable callback, final int mipLevel) {
    this.copyTextureToBuffer(source, destination, offset, callback, mipLevel, 0, 0, source.getWidth(mipLevel), source.getHeight(mipLevel));
  }

  @Override
  public void copyTextureToBuffer(
    final GpuTexture source,
    final GpuBuffer destination,
    final long offset,
    final Runnable callback,
    final int mipLevel,
    final int x,
    final int y,
    final int width,
    final int height
  ) {
    MemoryStack stack = MemoryStack.stackPush();

    try {
      org.lwjgl.vulkan.VkBufferImageCopy.Buffer copy = VkBufferImageCopy.calloc(1, stack);
      copy.bufferOffset(offset);
      VkImageSubresourceLayers subresource = copy.imageSubresource();
      subresource.aspectMask(VulkanConst.formatAspectMask(((VulkanGpuTexture)source).gpuFormat()));
      subresource.mipLevel(mipLevel);
      subresource.baseArrayLayer(0);
      subresource.layerCount(1);
      copy.imageOffset().set(x, y, 0);
      copy.imageExtent().set(width, height, 1);
      copy.bufferRowLength(width);
      copy.bufferImageHeight(height);
      VK12.vkCmdCopyImageToBuffer(this.commandBuffer(), ((VulkanGpuTexture)source).vkImage(), 1, ((VulkanGpuBuffer)destination).vkBuffer(), copy);
      this.memoryBarrier(stack);
    } catch (Throwable var15) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var14) {
          var15.addSuppressed(var14);
        }
      }

      throw var15;
    }

    if (stack != null) {
      stack.close();
    }

    this.queueForDestroy(callback::run);
  }

  @Override
  public void copyTextureToTexture(
    final GpuTexture source,
    final GpuTexture destination,
    final int mipLevel,
    final int destX,
    final int destY,
    final int sourceX,
    final int sourceY,
    final int width,
    final int height
  ) {
    VulkanGpuTexture vulkanSrc = (VulkanGpuTexture)source;
    VulkanGpuTexture vulkanDst = (VulkanGpuTexture)destination;
    MemoryStack stack = MemoryStack.stackPush();

    try {
      VkImageSubresourceLayers subresourceLayers = VkImageSubresourceLayers.calloc(stack);
      subresourceLayers.mipLevel(mipLevel);
      subresourceLayers.baseArrayLayer(0);
      subresourceLayers.layerCount(1);
      subresourceLayers.aspectMask(VulkanConst.formatAspectMask(((VulkanGpuTexture)source).gpuFormat()));
      org.lwjgl.vulkan.VkImageCopy.Buffer regions = VkImageCopy.calloc(1, stack);
      regions.srcOffset().set(sourceX, sourceY, 0);
      regions.dstOffset().set(destX, destY, 0);
      regions.extent().set(width, height, 1);
      regions.srcSubresource(subresourceLayers);
      regions.dstSubresource(subresourceLayers);
      VK12.vkCmdCopyImage(this.commandBuffer(), vulkanSrc.vkImage(), 1, vulkanDst.vkImage(), 1, regions);
      this.memoryBarrier(stack);
    } catch (Throwable var16) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var15) {
          var16.addSuppressed(var15);
        }
      }

      throw var16;
    }

    if (stack != null) {
      stack.close();
    }
  }

  public boolean awaitSubmitCompletion(final long submitIndex, final long timeoutNS) {
    if (this.completedSubmitIndex >= submitIndex) {
      return true;
    } else if (submitIndex == this.currentSubmitIndex) {
      if (timeoutNS == 0L) {
        return false;
      } else {
        throw new IllegalStateException("Cannot wait on a fence for the current submit");
      }
    } else {
      MemoryStack stack = MemoryStack.stackPush();

      boolean var9;
      try {
        VkSemaphoreWaitInfo waitInfo = VkSemaphoreWaitInfo.calloc(stack).sType$Default();
        waitInfo.pSemaphores(stack.longs(this.submitSemaphore));
        waitInfo.pValues(stack.longs(submitIndex));
        waitInfo.semaphoreCount(1);
        int result = VK12.vkWaitSemaphores(this.device.vkDevice(), waitInfo, timeoutNS);
        VulkanUtils.crashIfFailure(this.device, result, "Failed to wait for semaphore");
        boolean completed = result == 0;
        if (completed) {
          this.completedSubmitIndex = submitIndex;
        }

        var9 = completed;
      } catch (Throwable var11) {
        if (stack != null) {
          try {
            stack.close();
          } catch (Throwable var10) {
            var11.addSuppressed(var10);
          }
        }

        throw var11;
      }

      if (stack != null) {
        stack.close();
      }

      return var9;
    }
  }

  @Override
  public GpuFence createFence() {
    return new GpuFence() {
      private final long submitIndex;
      private boolean completed;

      {
        Objects.requireNonNull(VulkanCommandEncoder.this);
        this.submitIndex = VulkanCommandEncoder.this.currentSubmitIndex;
        this.completed = false;
      }

      @Override
      public boolean awaitCompletion(final long timeoutMs) {
        if (!this.completed) {
          this.completed = VulkanCommandEncoder.this.awaitSubmitCompletion(this.submitIndex, timeoutMs);
        }

        return this.completed;
      }

      @Override
      public void close() {
        this.completed = true;
      }
    };
  }

  public void writeTimestamp(final GpuQueryPool pool, final int index) {
    long queryPool = ((VulkanQueryPool)pool).vkQueryPool();
    VK12.vkResetQueryPool(this.device.vkDevice(), queryPool, index, 1);
    KHRSynchronization2.vkCmdWriteTimestamp2KHR(this.commandBuffer(), 65536L, queryPool, index);
  }

  public long getTimestampNow() {
    MemoryStack stack = MemoryStack.stackPush();

    long var5;
    try (VulkanQueryPool queryPool = (VulkanQueryPool)this.device.createTimestampQueryPool(1)) {
      VkCommandBuffer commandBuffer = this.allocateAndBeginTransientCommandBuffer();
      KHRSynchronization2.vkCmdWriteTimestamp2KHR(commandBuffer, 0L, queryPool.vkQueryPool(), 0);
      VulkanUtils.crashIfFailure(this.device, VK12.vkEndCommandBuffer(commandBuffer), "Failed to end VkCommandBuffer");

      try (VulkanQueue.Submission submit = this.device.graphicsQueue().beginSubmit()) {
        submit.executeCommands(commandBuffer);
      }

      LongBuffer timestampPtr = stack.callocLong(1);
      VulkanUtils.crashIfFailure(
        this.device, VK12.vkGetQueryPoolResults(this.device.vkDevice(), queryPool.vkQueryPool(), 0, 1, timestampPtr, 0L, 3), "Cannot fetch current timestamp"
      );
      var5 = timestampPtr.get(0);
    } catch (Throwable var12) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var7) {
          var12.addSuppressed(var7);
        }
      }

      throw var12;
    }

    if (stack != null) {
      stack.close();
    }

    return var5;
  }

  private static Vector4fc argb(final int color) {
    return new org.joml.Vector4f((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >>> 24) / 255.0F);
  }

  @Override
  public RenderPassBackend createRenderPass(final Supplier<String> label, final GpuTextureView colorTexture, final OptionalInt clearColor) {
    return this.createRenderPass(label, colorTexture, clearColor, null, OptionalDouble.empty());
  }

  @Override
  public RenderPassBackend createRenderPass(
    final Supplier<String> label,
    final GpuTextureView colorTexture,
    final OptionalInt clearColor,
    final @Nullable GpuTextureView depthTexture,
    final OptionalDouble clearDepth
  ) {
    RenderPassDescriptor descriptor = RenderPassDescriptor.create(label)
      .withColorAttachment(colorTexture, clearColor.isPresent() ? Optional.of(argb(clearColor.getAsInt())) : Optional.empty())
      .withRenderArea(new org.polyfrost.polydium.vulkan.compat.RenderArea(0, 0, colorTexture.getWidth(0), colorTexture.getHeight(0)));
    if (depthTexture != null) {
      descriptor.withDepthAttachment(depthTexture, clearDepth);
    }

    return this.createRenderPass(descriptor);
  }

  @Override
  public boolean isInRenderPass() {
    return this.currentRenderPass != null;
  }

  @Override
  public void clearColorTexture(final GpuTexture colorTexture, final int clearColor) {
    this.clearColorTexture(colorTexture, argb(clearColor));
  }

  @Override
  public void clearColorAndDepthTextures(final GpuTexture colorTexture, final int clearColor, final GpuTexture depthTexture, final double clearDepth) {
    this.clearColorAndDepthTextures(colorTexture, argb(clearColor), depthTexture, clearDepth);
  }

  @Override
  public void clearColorAndDepthTextures(
    final GpuTexture colorTexture,
    final int clearColor,
    final GpuTexture depthTexture,
    final double clearDepth,
    final int regionX,
    final int regionY,
    final int regionWidth,
    final int regionHeight
  ) {
    this.clearColorAndDepthTextures(colorTexture, argb(clearColor), depthTexture, clearDepth, regionX, regionY, regionWidth, regionHeight);
  }

  @Override
  public GpuBuffer.MappedView mapBuffer(final GpuBufferSlice buffer, final boolean read, final boolean write) {
    return ((VulkanGpuBuffer)buffer.buffer()).map(buffer.offset(), buffer.length(), read, write);
  }

  @Override
  public void writeToTexture(
    final GpuTexture destination,
    final com.mojang.blaze3d.platform.NativeImage source,
    final int mipLevel,
    final int depthOrLayer,
    final int destX,
    final int destY,
    final int width,
    final int height,
    final int sourceX,
    final int sourceY
  ) {
    int texelSize = source.format().components();
    long start = (sourceX + (long)sourceY * source.getWidth()) * texelSize;
    long length = ((long)(height - 1) * source.getWidth() + width) * texelSize;
    ByteBuffer data = org.lwjgl.system.MemoryUtil.memByteBuffer(source.getPointer() + start, (int)length);
    GpuBufferSlice staging = this.transientMemory.uploadStaging(data, texelSize, 16);
    this.copyBufferToTexture(staging, 0, 0, source.getWidth(), height, destination, destX, destY, width, height, mipLevel, depthOrLayer);
  }

  @Override
  public void writeToTexture(
    final GpuTexture destination,
    final ByteBuffer source,
    final com.mojang.blaze3d.platform.NativeImage.Format format,
    final int mipLevel,
    final int depthOrLayer,
    final int destX,
    final int destY,
    final int width,
    final int height
  ) {
    this.writeToTexture(destination, source, mipLevel, depthOrLayer, destX, destY, width, height);
  }

  @Override
  public void presentTexture(final GpuTextureView texture) {
    this.device.presentTexture(texture);
  }

  @Override
  public com.mojang.blaze3d.systems.GpuQuery timerQueryBegin() {
    return new CpuTimerQuery();
  }

  @Override
  public void timerQueryEnd(final com.mojang.blaze3d.systems.GpuQuery query) {
    ((CpuTimerQuery)query).elapsed = System.nanoTime() - ((CpuTimerQuery)query).start;
  }

  private static final class CpuTimerQuery implements com.mojang.blaze3d.systems.GpuQuery {
    private final long start = System.nanoTime();
    private long elapsed = -1L;

    @Override
    public java.util.OptionalLong getValue() {
      return this.elapsed < 0L ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(this.elapsed);
    }

    @Override
    public void close() {
    }
  }
}
