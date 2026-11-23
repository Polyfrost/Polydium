package org.polyfrost.polydium.vulkan.compat;

import com.mojang.blaze3d.systems.*;

import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public class RenderPassDescriptor {
  private static @Nullable List<RenderPassDescriptor.@Nullable Attachment<Optional<Vector4fc>>> lastColors;
  private static RenderPassDescriptor.@Nullable Attachment<OptionalDouble> lastDepth;
  private final Supplier<String> label;
  public List<RenderPassDescriptor.@Nullable Attachment<Optional<Vector4fc>>> colorAttachments = Collections.emptyList();
  public RenderPassDescriptor.@Nullable Attachment<OptionalDouble> depthAttachment;
  public @Nullable RenderArea renderArea;
  private @Nullable List<RenderPassDescriptor.@Nullable Attachment<Optional<Vector4fc>>> sharedColors = this.colorAttachments;

  public static RenderPassDescriptor create(final Supplier<String> label) {
    return new RenderPassDescriptor(label);
  }

  private RenderPassDescriptor(final Supplier<String> label) {
    this.label = label;
  }

  public RenderPassDescriptor withColorAttachment(final GpuTextureView textureView) {
    return this.withColorAttachment(textureView, Optional.empty());
  }

  public RenderPassDescriptor withColorAttachment(final GpuTextureView textureView, final Optional<Vector4fc> clearValue) {
    if (clearValue.isEmpty() && this.colorAttachments.isEmpty() && this.colorAttachments == this.sharedColors) {
      List<RenderPassDescriptor.@Nullable Attachment<Optional<Vector4fc>>> colors = lastColors;
      if (colors == null || colors.get(0).textureView() != textureView) {
        colors = Collections.singletonList(new RenderPassDescriptor.Attachment<Optional<Vector4fc>>(textureView, Optional.empty()));
        lastColors = colors;
      }
      this.colorAttachments = colors;
      this.sharedColors = colors;
      return this;
    }
    this.addColorAttachment(new RenderPassDescriptor.Attachment<>(textureView, clearValue));
    return this;
  }

  public RenderPassDescriptor withUnusedColorAttachment() {
    this.addColorAttachment(null);
    return this;
  }

  private void addColorAttachment(final RenderPassDescriptor.@Nullable Attachment<Optional<Vector4fc>> attachment) {
    if (this.colorAttachments == this.sharedColors) {
      List<RenderPassDescriptor.@Nullable Attachment<Optional<Vector4fc>>> colors = new ArrayList<>(this.colorAttachments.size() + 1);
      colors.addAll(this.colorAttachments);
      this.colorAttachments = colors;
      this.sharedColors = null;
    }
    this.colorAttachments.add(attachment);
  }

  public RenderPassDescriptor withDepthAttachment(final GpuTextureView textureView) {
    return this.withDepthAttachment(textureView, OptionalDouble.empty());
  }

  public RenderPassDescriptor withDepthAttachment(final GpuTextureView textureView, final OptionalDouble clearValue) {
    RenderPassDescriptor.Attachment<OptionalDouble> depth = lastDepth;
    if (depth == null || depth.textureView() != textureView || !depth.clearValue().equals(clearValue)) {
      depth = new RenderPassDescriptor.Attachment<>(textureView, clearValue);
      lastDepth = depth;
    }
    this.depthAttachment = depth;
    return this;
  }

  public RenderPassDescriptor withRenderArea(final RenderArea renderArea) {
    this.renderArea = renderArea;
    return this;
  }

  public Supplier<String> label() {
    return this.label;
  }

  public List<RenderPassDescriptor.@Nullable Attachment<Optional<Vector4fc>>> colorAttachments() {
    return this.colorAttachments;
  }

  public RenderPassDescriptor.@Nullable Attachment<OptionalDouble> depthAttachment() {
    return this.depthAttachment;
  }

  public record Attachment<T>(GpuTextureView textureView, T clearValue) {
  }
}
