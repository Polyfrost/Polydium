package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
//? if >= 26.1
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public final class VulkanActive {
  public static @Nullable VulkanBackend backend;

  public static boolean zZeroToOne() {
    //? if >= 26.1 {
    return RenderSystem.getDevice().isZZeroToOne();
    //?} else {
    /*return false;
    *///?}
  }

  public static boolean active;

  public static Scissor postPassScissor;

  public static final Set<RenderPipeline> CULL_FRONT = Collections.newSetFromMap(new IdentityHashMap<>());

  public static final Map<RenderPipeline, ColorTargetState[]> COLOR_TARGETS = new IdentityHashMap<>();

  public interface Scissor {
    int x();

    int y();

    int width();

    int height();
  }

  private VulkanActive() {
  }

  public static int textureId(Object texture) {
    return active ? System.identityHashCode(texture) : ((GlTexture) texture).glId();
  }
}
