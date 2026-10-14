package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class VulkanActive {
  public static @org.jspecify.annotations.Nullable VulkanBackend backend;

  public static boolean zZeroToOne() {
    //? if >= 26.1 {
    return com.mojang.blaze3d.systems.RenderSystem.getDevice().isZZeroToOne();
    //?} else {
    /*return false;
    *///?}
  }

  public static boolean active;

  public static Scissor postPassScissor;

  public static final Set<RenderPipeline> CULL_FRONT = Collections.newSetFromMap(new IdentityHashMap<>());

  public static final java.util.Map<RenderPipeline, com.mojang.blaze3d.pipeline.ColorTargetState[]> COLOR_TARGETS = new IdentityHashMap<>();

  public interface Scissor {
    int x();

    int y();

    int width();

    int height();
  }

  private VulkanActive() {
  }

  public static void _enableBlend() {
    if (!active) GlStateManager._enableBlend();
  }

  public static void _disableBlend() {
    if (!active) GlStateManager._disableBlend();
  }

  public static int textureId(Object texture) {
    return active ? System.identityHashCode(texture) : ((GlTexture) texture).glId();
  }
}
