package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.render.TextureSetup;
import org.lwjgl.glfw.GLFW;
import org.polyfrost.polydium.vulkan.compat.GpuSurface;
import org.polyfrost.polydium.vulkan.compat.SurfaceException;
import org.slf4j.Logger;

public final class OwoVulkan {
  private static final MethodHandle TEXTURE_SETUP;
  private static final MethodHandle BLUR_SETUP_OF;
  private static final MethodHandle INPUT;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      Class<?> draw = Class.forName("net.minecraft.client.gui.render.GuiRenderer$Draw");
      TEXTURE_SETUP = MethodHandles.privateLookupIn(draw, lookup)
        .findVirtual(draw, "textureSetup", MethodType.methodType(TextureSetup.class))
        .asType(MethodType.methodType(TextureSetup.class, Object.class));
      Class<?> blur = Class.forName("io.wispforest.owo.ui.renderstate.BlurQuadElementRenderState");
      BLUR_SETUP_OF = lookup.findStatic(blur, "getBlurSetupOf", MethodType.methodType(Class.forName(blur.getName() + "$BlurSetup"), TextureSetup.class))
        .asType(MethodType.methodType(Object.class, TextureSetup.class));
      INPUT = lookup.findStaticGetter(blur, "input", RenderTarget.class);
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private OwoVulkan() {
  }

  public static boolean isBlur(Object draw) {
    try {
      return BLUR_SETUP_OF.invokeExact((TextureSetup) TEXTURE_SETUP.invokeExact(draw)) != null;
    } catch (Throwable t) {
      throw new IllegalStateException(t);
    }
  }

  public static RenderTarget blurInput() {
    try {
      return (RenderTarget) INPUT.invokeExact();
    } catch (Throwable t) {
      throw new IllegalStateException(t);
    }
  }

  public static void setInRenderPass(Object backend, boolean inRenderPass) {
    if (!VulkanActive.active) GlHolder.set(backend, inRenderPass);
  }

  public static void copyTextureToTexture(
    CommandEncoder encoder, GpuTexture source, GpuTexture destination, int mipLevel, int destX, int destY, int sourceX, int sourceY, int width, int height
  ) {
    if (!VulkanActive.active) encoder.copyTextureToTexture(source, destination, mipLevel, destX, destY, sourceX, sourceY, width, height);
  }

  private static final Logger LOGGER = LogUtils.getLogger();
  private static final Map<Long, BraidSurface> BRAID_SURFACES = new HashMap<>();

  private static final class BraidSurface {
    final GpuSurface surface;
    boolean valid;

    BraidSurface(long window) {
      this.surface = new GpuSurface(new VulkanGpuSurface(VulkanDevice.active, window));
    }
  }

  public static long braidCreateWindow(int width, int height, String title) {
    GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
    long handle = GLFW.glfwCreateWindow(width, height, title, 0L, 0L);
    if (handle == 0L) {
      throw new UnsupportedOperationException("Failed to create a GLFW window");
    }
    return handle;
  }

  public static void braidResize(long window) {
    BraidSurface braid = BRAID_SURFACES.get(window);
    if (braid != null) braid.valid = false;
  }

  public static void braidPresent(long window, RenderTarget target) {
    int[] width = new int[1];
    int[] height = new int[1];
    GLFW.glfwGetFramebufferSize(window, width, height);
    if (width[0] == 0 || height[0] == 0) return;

    BraidSurface braid = BRAID_SURFACES.computeIfAbsent(window, BraidSurface::new);
    if (!braid.valid || braid.surface.isSuboptimal()) {
      try {
        braid.surface.configure(new GpuSurface.Configuration(
          width[0], height[0], GpuSurface.PresentMode.getSupportedVsyncMode(braid.surface.supportedPresentModes(), false)));
        braid.valid = true;
      } catch (SurfaceException e) {
        LOGGER.warn("Failed to resize braid window");
        return;
      }
    }

    try {
      braid.surface.acquireNextTexture();
    } catch (SurfaceException e) {
      LOGGER.warn("Failed to acquire texture");
      braid.valid = false;
      return;
    }

    VulkanCommandEncoder encoder = VulkanDevice.active.createCommandEncoder();
    braid.surface.blitFromTexture(encoder, target.getColorTextureView());
    encoder.submit();
    braid.surface.present();
  }

  public static void glfwDestroyWindow(long window) {
    BraidSurface braid = BRAID_SURFACES.remove(window);
    if (braid != null) braid.surface.close();
    GLFW.glfwDestroyWindow(window);
  }

  private static final class GlHolder {
    private static final MethodHandle SET_IN_RENDER_PASS;

    static {
      try {
        Class<?> accessor = Class.forName("io.wispforest.owo.mixin.ui.access.GlCommandEncoderAccessor");
        SET_IN_RENDER_PASS = MethodHandles.lookup()
          .findVirtual(accessor, "owo$setInRenderPass", MethodType.methodType(void.class, boolean.class))
          .asType(MethodType.methodType(void.class, Object.class, boolean.class));
      } catch (ReflectiveOperationException e) {
        throw new ExceptionInInitializerError(e);
      }
    }

    static void set(Object backend, boolean inRenderPass) {
      try {
        SET_IN_RENDER_PASS.invokeExact(backend, inRenderPass);
      } catch (Throwable t) {
        throw new IllegalStateException(t);
      }
    }
  }
}
