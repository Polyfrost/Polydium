package org.polyfrost.polydium.vulkan;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class SodiumExtraMac {
  private static final MethodHandle IS_ENABLED;
  public static long window;

  static {
    MethodHandle handle = null;
    try {
      handle = MethodHandles.publicLookup().findStatic(
        Class.forName("me.flashyreese.mods.sodiumextra.client.util.MacReducedResolution"), "isEnabled", MethodType.methodType(boolean.class)
      );
    } catch (ReflectiveOperationException | LinkageError ignored) {
    }
    IS_ENABLED = handle;
  }

  public static boolean enabled() {
    try {
      return IS_ENABLED != null && (boolean) IS_ENABLED.invokeExact();
    } catch (Throwable t) {
      return false;
    }
  }

  public static void reduceSwapchain(long window, int[] width, int[] height) {
    int[] windowWidth = new int[1];
    int[] windowHeight = new int[1];
    org.lwjgl.glfw.GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
    if (isHighDpi(width[0], height[0], windowWidth[0], windowHeight[0]) && enabled()) {
      width[0] = Math.max(1, width[0] / 2);
      height[0] = Math.max(1, height[0] / 2);
    }
  }

  public static boolean isHighDpi(long window) {
    int[] framebufferWidth = new int[1];
    int[] framebufferHeight = new int[1];
    int[] windowWidth = new int[1];
    int[] windowHeight = new int[1];
    org.lwjgl.glfw.GLFW.glfwGetFramebufferSize(window, framebufferWidth, framebufferHeight);
    org.lwjgl.glfw.GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
    return isHighDpi(framebufferWidth[0], framebufferHeight[0], windowWidth[0], windowHeight[0]);
  }

  public static boolean isHighDpi(int framebufferWidth, int framebufferHeight, int windowWidth, int windowHeight) {
    return windowWidth > 0 && windowHeight > 0 && (framebufferWidth > windowWidth || framebufferHeight > windowHeight);
  }
}
