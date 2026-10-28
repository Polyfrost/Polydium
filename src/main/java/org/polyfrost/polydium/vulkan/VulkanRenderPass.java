package org.polyfrost.polydium.vulkan;

import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.polyfrost.polydium.vulkan.compat.RenderArea;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import org.polyfrost.polydium.vulkan.compat.GpuQueryPool;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.polyfrost.polydium.vulkan.checkpoints.CheckpointExtension;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTMultiDraw;
import org.lwjgl.vulkan.KHRPushDescriptor;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDrawIndexedIndirectCommand;
import org.lwjgl.vulkan.VkDrawIndirectCommand;
import org.lwjgl.vulkan.VkMultiDrawIndexedInfoEXT;
import org.lwjgl.vulkan.VkMultiDrawInfoEXT;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import org.lwjgl.vulkan.VkViewport.Buffer;

public class VulkanRenderPass implements RenderPassBackend {
  public static final boolean VALIDATION = SharedConstants.IS_RUNNING_IN_IDE;
  private final VulkanDevice device;
  private final VulkanCommandEncoder encoder;
  private final CheckpointExtension.CheckpointStorage checkpointStorage;
  private final @Nullable RenderArea renderArea;
  private final int outputWidth;
  private final int outputHeight;
  private final boolean hasDepth;
  private final Supplier<String> label;
  protected int pushedDebugGroups = 0;
  private final VkCommandBuffer commandBuffer;
  protected @Nullable VulkanRenderPipeline pipeline;
  private boolean anyDescriptorDirty = false;
  protected final HashMap<String, GpuBufferSlice> uniforms = new HashMap<>();
  protected final HashMap<String, VulkanRenderPass.TextureViewAndSampler> textures = new HashMap<>();
  private Object[] uniformSlots = VulkanDynamicState.NO_SLOTS;
  private Object[] textureSlots = VulkanDynamicState.NO_SLOTS;

  public VulkanRenderPass(
    final VulkanDevice device,
    final VulkanCommandEncoder encoder,
    final VkCommandBuffer commandBuffer,
    final CheckpointExtension.CheckpointStorage checkpointStorage,
    final RenderArea renderArea,
    final int outputWidth,
    final int outputHeight,
    final boolean hasDepth,
    final Supplier<String> label
  ) {
    this.device = device;
    this.encoder = encoder;
    this.commandBuffer = commandBuffer;
    this.checkpointStorage = checkpointStorage;
    this.renderArea = renderArea;
    this.outputWidth = outputWidth;
    this.outputHeight = outputHeight;
    this.hasDepth = hasDepth;
    this.label = label;
    MemoryStack stack = MemoryStack.stackPush();

    try {
      Buffer viewport = VkViewport.calloc(1, stack);
      viewport.x(0.0F);
      viewport.y(0.0F);
      viewport.width(outputWidth);
      viewport.height(outputHeight);
      viewport.minDepth(0.0F);
      viewport.maxDepth(1.0F);
      VK12.vkCmdSetViewport(this.commandBuffer(), 0, viewport);
      setScissor(stack, this.commandBuffer(), renderArea.x(), renderArea.y(), renderArea.width(), renderArea.height());
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

  private VkCommandBuffer commandBuffer() {
    return this.commandBuffer;
  }

  @Override
  public void pushDebugGroup(final Supplier<String> label) {
    this.pushedDebugGroups++;
    this.device.instance().debug().beginDebugGroup(this.commandBuffer(), label);
  }

  @Override
  public void popDebugGroup() {
    if (this.pushedDebugGroups == 0) {
      throw new IllegalStateException("Can't pop more debug groups than was pushed!");
    } else {
      this.pushedDebugGroups--;
      this.device.instance().debug().endDebugGroup(this.commandBuffer());
    }
  }

  @Override
  public void setPipeline(final RenderPipeline pipeline) {
    VulkanRenderPipeline previous = this.pipeline;
    this.pipeline = this.device.getOrCompilePipeline(pipeline);
    if (!this.pipeline.isValid()) {
      throw new IllegalStateException("Pipeline is not valid (may contain invalid shaders?)");
    } else {
      if (this.pipeline != previous) {
        this.anyDescriptorDirty = true;
      }
      VK12.vkCmdBindPipeline(this.commandBuffer(), 0, this.hasDepth ? this.pipeline.withDepthPipeline() : this.pipeline.withoutDepthPipeline());
    }
  }

  @Override
  public void bindTexture(final String name, final @Nullable GpuTextureView textureView, final @Nullable GpuSampler sampler) {
    if (textureView != null && sampler != null) {
      VulkanRenderPass.TextureViewAndSampler texture = new VulkanRenderPass.TextureViewAndSampler((VulkanGpuTextureView)textureView, (VulkanGpuSampler)sampler);
      if (!texture.equals(this.textures.put(name, texture))) {
        this.textureSlots = VulkanDynamicState.setSlot(this.textureSlots, name, texture);
        this.anyDescriptorDirty = true;
      }
    } else if (textureView == null && sampler == null) {
      if (this.textures.remove(name) != null) {
        this.textureSlots = VulkanDynamicState.setSlot(this.textureSlots, name, null);
      }
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void setUniform(final String name, final GpuBuffer value) {
    GpuBufferSlice slice = value.slice();
    if (!slice.equals(this.uniforms.put(name, slice))) {
      this.uniformSlots = VulkanDynamicState.setSlot(this.uniformSlots, name, slice);
      this.anyDescriptorDirty = true;
    }
  }

  @Override
  public void setUniform(final String name, final GpuBufferSlice value) {
    if (!value.equals(this.uniforms.put(name, value))) {
      this.uniformSlots = VulkanDynamicState.setSlot(this.uniformSlots, name, value);
      this.anyDescriptorDirty = true;
    }
  }

  @Override
  public void enableScissor(final int x, final int y, final int width, final int height) {
    MemoryStack stack = MemoryStack.stackPush();

    try {
      setScissor(stack, this.commandBuffer(), x, y, width, height);
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

  private static void setScissor(final MemoryStack stack, final VkCommandBuffer commandBuffer, int x, int y, int width, int height) {
    if (x < 0) {
      width = Math.max(0, width + x);
      x = 0;
    }
    if (y < 0) {
      height = Math.max(0, height + y);
      y = 0;
    }
    org.lwjgl.vulkan.VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
    scissor.offset().set(x, y);
    scissor.extent().set(width, height);
    VK12.vkCmdSetScissor(commandBuffer, 0, scissor);
  }

  @Override
  public void disableScissor() {
    if (this.renderArea != null) {
      this.enableScissor(this.renderArea.x(), this.renderArea.y(), this.renderArea.width(), this.renderArea.height());
    } else {
      this.enableScissor(0, 0, this.outputWidth, this.outputHeight);
    }
  }

  @Override
  public void setVertexBuffer(final int slot, final GpuBuffer vertexBuffer) {
    MemoryStack stack = MemoryStack.stackPush();

    try {
      VK12.vkCmdBindVertexBuffers(this.commandBuffer(), slot, stack.longs(((VulkanGpuBuffer)vertexBuffer).vkBuffer()), stack.longs(0L));
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
  public void setIndexBuffer(final GpuBuffer indexBuffer, final VertexFormat.IndexType indexType) {
    int type = switch (indexType) {
      case SHORT -> 0;
      case INT -> 1;
    };
    VK12.vkCmdBindIndexBuffer(this.commandBuffer(), ((VulkanGpuBuffer)indexBuffer).vkBuffer(), 0L, type);
  }

  @Override
  public void drawIndexed(final int baseVertex, final int firstIndex, final int indexCount, final int instanceCount) {
    this.drawIndexed(indexCount, instanceCount, firstIndex, baseVertex, 0);
  }

  public void drawIndexed(final int indexCount, final int instanceCount, final int firstIndex, final int vertexOffset, final int firstInstance) {
    if (this.pipeline != null && this.pipeline.isValid()) {
      this.pushDescriptors();
      VK12.vkCmdDrawIndexed(this.commandBuffer(), indexCount, instanceCount, firstIndex, vertexOffset, firstInstance);
    } else {
      throw new IllegalStateException("Pipeline is missing or not valid");
    }
  }

  public void multiDrawIndexed(final IntBuffer drawParameters, final int instanceCount, final int firstInstance, final int drawCount) {
    if (this.pipeline != null && this.pipeline.isValid()) {
      this.pushDescriptors();
      EXTMultiDraw.nvkCmdDrawMultiIndexedEXT(
        this.commandBuffer(), drawCount, MemoryUtil.memAddress(drawParameters), instanceCount, firstInstance, VkMultiDrawIndexedInfoEXT.SIZEOF, 0L
      );
    } else {
      throw new IllegalStateException("Pipeline is missing or not valid");
    }
  }

  public void multiDrawIndexed(final PointerBuffer firstIndexOffsets, final IntBuffer indexCounts, final IntBuffer vertexOffsets, final int drawCount) {
    throw new UnsupportedOperationException("Vulkan does not support the multiDrawDirectSeparate device feature");
  }

  public void pushConstants(final long address, final int size) {
    VK12.nvkCmdPushConstants(this.commandBuffer(), this.pipeline.pipelineLayout(), VK12.VK_SHADER_STAGE_ALL, 0, size, address);
  }

  public void drawIndexedIndirect(final GpuBufferSlice commands, final int drawCount) {
    if (this.pipeline != null && this.pipeline.isValid()) {
      this.pushDescriptors();
      VK12.vkCmdDrawIndexedIndirect(
        this.commandBuffer(), ((VulkanGpuBuffer)commands.buffer()).vkBuffer(), commands.offset(), drawCount, VkDrawIndexedIndirectCommand.SIZEOF
      );
    } else {
      throw new IllegalStateException("Pipeline is missing or not valid");
    }
  }

  @Override
  public <T> void drawMultipleIndexed(
    final Collection<RenderPass.Draw<T>> draws,
    final @Nullable GpuBuffer defaultIndexBuffer,
    final VertexFormat.@Nullable IndexType defaultIndexType,
    final Collection<String> dynamicUniforms,
    final T uniformArgument
  ) {
    multiDrawing = this;
    try {
    for (RenderPass.Draw<T> draw : draws) {
      BiConsumer<T, RenderPass.UniformUploader> uniformUploaderConsumer = draw.uniformUploaderConsumer();
      if (uniformUploaderConsumer != null) {
        uniformUploaderConsumer.accept(uniformArgument, this::setUniform);
      }

      assert draw.indexBuffer() != null || defaultIndexBuffer != null;

      assert draw.indexType() != null || defaultIndexType != null;

      this.setIndexBuffer(draw.indexBuffer() == null ? defaultIndexBuffer : draw.indexBuffer(), draw.indexType() == null ? defaultIndexType : draw.indexType());
      this.setVertexBuffer(draw.slot(), draw.vertexBuffer());
      //? if >= 26.1 {
      this.drawIndexed(draw.indexCount(), 1, draw.firstIndex(), draw.baseVertex(), 0);
      //?} else {
      /*this.drawIndexed(draw.indexCount(), 1, draw.firstIndex(), 0, 0);
      *///?}
    }
    } finally {
      multiDrawing = null;
      this.multiDrawViews.forEach(GpuTextureView::close);
      this.multiDrawViews.clear();
    }
  }

  public static @Nullable VulkanRenderPass multiDrawing;
  private final ArrayList<GpuTextureView> multiDrawViews = new ArrayList<>();

  public void rebindSampler0(final GpuTexture texture) {
    VulkanRenderPass.TextureViewAndSampler bound = this.textures.get("Sampler0");
    if (bound == null) return;
    GpuTextureView view = this.device.createTextureView(texture);
    this.multiDrawViews.add(view);
    this.bindTexture("Sampler0", view, bound.sampler());
  }

  @Override
  public void draw(final int firstVertex, final int vertexCount) {
    VulkanActive.Scissor scissor = VulkanActive.postPassScissor;
    if (scissor != null) {
      this.enableScissor(scissor.x(), scissor.y(), scissor.width(), scissor.height());
    }

    this.draw(vertexCount, 1, firstVertex, 0);
  }

  public void draw(final int vertexCount, final int instanceCount, final int firstVertex, final int firstInstance) {
    if (this.pipeline != null && this.pipeline.isValid()) {
      this.pushDescriptors();
      VK12.vkCmdDraw(this.commandBuffer(), vertexCount, instanceCount, firstVertex, firstInstance);
    }
  }

  public void multiDraw(final IntBuffer drawParameters, final int instanceCount, final int firstInstance, final int drawCount) {
    if (this.pipeline != null && this.pipeline.isValid()) {
      this.pushDescriptors();
      EXTMultiDraw.nvkCmdDrawMultiEXT(
        this.commandBuffer(), drawCount, MemoryUtil.memAddress(drawParameters), instanceCount, firstInstance, VkMultiDrawInfoEXT.SIZEOF
      );
    } else {
      throw new IllegalStateException("Pipeline is missing or not valid");
    }
  }

  public void multiDraw(final IntBuffer firstVertices, final IntBuffer vertexCounts, final int drawCount) {
    throw new UnsupportedOperationException("Vulkan does not support the multiDrawDirectSeparate device feature");
  }

  public void drawIndirect(final GpuBufferSlice commands, final int drawCount) {
    if (this.pipeline != null && this.pipeline.isValid()) {
      this.pushDescriptors();
      VK12.vkCmdDrawIndirect(this.commandBuffer(), ((VulkanGpuBuffer)commands.buffer()).vkBuffer(), commands.offset(), drawCount, VkDrawIndirectCommand.SIZEOF);
    } else {
      throw new IllegalStateException("Pipeline is missing or not valid");
    }
  }

  private void pushDescriptors() {
    if (this.anyDescriptorDirty) {
      if (VALIDATION) {
        for (RenderPipeline.UniformDescription uniform : this.pipeline.info().getUniforms()) {
          GpuBufferSlice value = this.uniforms.get(uniform.name());
          if (value == null) {
            throw new IllegalStateException("Missing uniform " + uniform.name() + " (should be " + uniform.type() + ")");
          }

          if (uniform.type() == UniformType.UNIFORM_BUFFER) {
            if (value.buffer().isClosed()) {
              throw new IllegalStateException("Uniform buffer " + uniform.name() + " is already closed");
            }

            if ((value.buffer().usage() & 128) == 0) {
              throw new IllegalStateException("Uniform buffer " + uniform.name() + " must have GpuBuffer.USAGE_UNIFORM");
            }
          }

          if (uniform.type() == UniformType.TEXEL_BUFFER) {
            if (value.offset() != 0L || value.length() != value.buffer().size()) {
              throw new IllegalStateException("Uniform texel buffers do not support a slice of a buffer, must be entire buffer");
            }

            if ((value.buffer().usage() & 256) == 0) {
              throw new IllegalStateException("Uniform texel buffer " + uniform.name() + " must have GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER");
            }

            if (uniform.textureFormat() == null) {
              throw new IllegalStateException("Invalid uniform texel buffer " + uniform.name() + " (missing a texture format)");
            }
          }
        }
      }

      assert this.pipeline != null;

      VulkanBindGroupLayout layout = this.pipeline.layout();
      MemoryStack stack = MemoryStack.stackPush();

      try {
        org.lwjgl.vulkan.VkWriteDescriptorSet.Buffer writes = VkWriteDescriptorSet.calloc(layout.entries().size(), stack);

        for (int i = 0; i < layout.entries().size(); i++) {
          VulkanBindGroupLayout.Entry entry = layout.entries().get(i);
          VkWriteDescriptorSet set = ((VkWriteDescriptorSet)writes.get()).sType$Default();
          set.dstBinding(i);
          set.dstArrayElement(0);
          set.descriptorCount(1);
          if (entry.type() == VulkanBindGroupLayout.VulkanBindGroupEntryType.UNIFORM_BUFFER) {
            GpuBufferSlice buffer = this.uniforms.get(entry.name());
            if (buffer == null && !this.uniforms.isEmpty() && this.pipeline.info().getUniforms().stream().noneMatch(u -> u.name().equals(entry.name()))) {
              buffer = this.uniforms.values().iterator().next();
            }

            if (buffer == null) {
              throw new IllegalStateException("Missing uniform " + entry.name() + " (should be " + entry.type() + ")");
            }

            org.lwjgl.vulkan.VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
            bufferInfo.buffer(((VulkanGpuBuffer)buffer.buffer()).vkBuffer());
            bufferInfo.offset(buffer.offset());
            bufferInfo.range(buffer.length());
            set.descriptorType(6);
            set.pBufferInfo(bufferInfo);
          } else if (entry.type() == VulkanBindGroupLayout.VulkanBindGroupEntryType.SAMPLED_IMAGE) {
            VulkanRenderPass.TextureViewAndSampler valuex = this.textures.get(entry.name());
            // OpenGL silently tolerates unbound samplers, so mods may omit some (e.g. a lightmap the fragment shader never reads)
            if (valuex == null && !this.textures.isEmpty()) {
              valuex = this.textures.values().iterator().next();
            }

            if (valuex == null) {
              throw new IllegalStateException("Missing sampler " + entry.name());
            }

            org.lwjgl.vulkan.VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack);
            imageInfo.sampler(valuex.sampler.vkSampler());
            imageInfo.imageView(valuex.view.vkImageView());
            imageInfo.imageLayout(1);
            set.descriptorType(1);
            set.pImageInfo(imageInfo);
          } else if (entry.type() == VulkanBindGroupLayout.VulkanBindGroupEntryType.TEXEL_BUFFER) {
            GpuBufferSlice valuex = this.uniforms.get(entry.name());
            if (valuex == null) {
              throw new IllegalStateException("Missing uniform " + entry.name() + " (should be " + entry.type() + ")");
            }

            LongBuffer bufferViewPtr = stack.callocLong(1);
            MemoryStack var9 = stack.push();

            try {
              assert entry.texelBufferFormat() != null;

              VkBufferViewCreateInfo viewCreateInfo = VkBufferViewCreateInfo.calloc(stack).sType$Default();
              viewCreateInfo.buffer(((VulkanGpuBuffer)valuex.buffer()).vkBuffer());
              viewCreateInfo.offset(valuex.offset());
              viewCreateInfo.range(valuex.length());
              viewCreateInfo.format(VulkanConst.toVk(entry.texelBufferFormat()));
              VulkanUtils.crashIfFailure(
                this.device,
                VK12.vkCreateBufferView(this.device.vkDevice(), viewCreateInfo, null, bufferViewPtr),
                "Couldn't create buffer view for texel buffer"
              );
              long bufferViewHandle = bufferViewPtr.get(0);
              this.encoder.queueForDestroy(() -> VK12.vkDestroyBufferView(this.device.vkDevice(), bufferViewHandle, null));
            } catch (Throwable var15) {
              if (var9 != null) {
                try {
                  var9.close();
                } catch (Throwable var14) {
                  var15.addSuppressed(var14);
                }
              }

              throw var15;
            }

            if (var9 != null) {
              var9.close();
            }

            set.descriptorType(4);
            set.pTexelBufferView(bufferViewPtr);
          }
        }

        KHRPushDescriptor.vkCmdPushDescriptorSetKHR(
          this.commandBuffer(), 0, this.pipeline.pipelineLayout(), 0, (org.lwjgl.vulkan.VkWriteDescriptorSet.Buffer)writes.flip()
        );
      } catch (Throwable var16) {
        if (stack != null) {
          try {
            stack.close();
          } catch (Throwable var13) {
            var16.addSuppressed(var13);
          }
        }

        throw var16;
      }

      if (stack != null) {
        stack.close();
      }

      this.anyDescriptorDirty = false;
    }
  }

  public void writeTimestamp(final GpuQueryPool pool, final int index) {
    long queryPool = ((VulkanQueryPool)pool).vkQueryPool();
    VK12.vkResetQueryPool(this.device.vkDevice(), queryPool, index, 1);
    KHRSynchronization2.vkCmdWriteTimestamp2KHR(this.commandBuffer(), 65536L, queryPool, index);
  }

  private boolean closed;

  @Override
  public void close() {
    if (!this.closed) {
      this.closed = true;
      this.encoder.submitRenderPass();
    }
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  public Supplier<String> getLabel() {
    return this.label;
  }

  public record TextureViewAndSampler(VulkanGpuTextureView view, VulkanGpuSampler sampler) {
  }
}
