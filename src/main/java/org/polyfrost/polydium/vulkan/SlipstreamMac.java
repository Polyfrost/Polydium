package org.polyfrost.polydium.vulkan;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.polyfrost.polydium.compat.Mods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optional bridge to Slipstream's macOS IOSurface presenter ({@code dev.rdh.slipstream.VulkanPresenter}).
 *
 * <p>On macOS, presenting through MoltenVK's swapchain makes every frame's submission wait for a
 * {@code CAMetalLayer} drawable, so the render thread ends up blocked in {@code awaitSubmitCompletion}
 * for most of the frame. Slipstream renders into IOSurfaces imported with {@code VK_EXT_metal_objects}
 * and hands the newest finished one to the window's layer without blocking, which removes that stall.
 *
 * <p>Slipstream hooks Mojang's own Vulkan backend on 26.x, but on the 1.21.11/26.1 backport the
 * backend lives in this package, so Polydium wires the presenter in itself. Everything is reflective so
 * Slipstream stays an optional runtime companion. Disable with {@code -Dpolydium.slipstreamPresent=false}.
 */
public final class SlipstreamMac {
  private static final Logger LOGGER = LoggerFactory.getLogger("polydium/slipstream");
  private static final MethodHandle CREATE_DEVICE;
  private static final MethodHandle CREATE;
  private static final MethodHandle ACQUIRE;
  private static final MethodHandle IMAGE;
  private static final MethodHandle SEMAPHORE;
  private static final MethodHandle SUBMITTED;
  private static final MethodHandle PRESENT;
  private static final MethodHandle CLOSE;

  static {
    MethodHandle createDevice = null, create = null, acquire = null, image = null, semaphore = null, submitted = null, present = null, close = null;
    if (Platform.get() == Platform.MACOSX && Mods.loaded("slipstream") && !"false".equals(System.getProperty("polydium.slipstreamPresent"))) {
      try {
        Class<?> presenter = Class.forName("dev.rdh.slipstream.VulkanPresenter");
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        createDevice = lookup.findStatic(presenter, "createDevice",
          MethodType.methodType(int.class, VkPhysicalDevice.class, VkDeviceCreateInfo.class, VkAllocationCallbacks.class, PointerBuffer.class));
        create = lookup.findStatic(presenter, "create",
          MethodType.methodType(presenter, VkDevice.class, long.class, boolean.class, int.class, int.class, int.class));
        acquire = instance(lookup, presenter, "acquire", boolean.class);
        image = instance(lookup, presenter, "image", long.class);
        semaphore = instance(lookup, presenter, "semaphore", long.class);
        submitted = instance(lookup, presenter, "submitted", long.class);
        present = instance(lookup, presenter, "present", void.class);
        close = instance(lookup, presenter, "close", void.class);
        create = create.asType(MethodType.methodType(Object.class, VkDevice.class, long.class, boolean.class, int.class, int.class, int.class));
      } catch (ReflectiveOperationException | LinkageError e) {
        LOGGER.warn("Slipstream is loaded but its Vulkan presenter API is unavailable; keeping the MoltenVK swapchain", e);
        createDevice = create = null;
      }
    }
    CREATE_DEVICE = createDevice;
    CREATE = create;
    ACQUIRE = acquire;
    IMAGE = image;
    SEMAPHORE = semaphore;
    SUBMITTED = submitted;
    PRESENT = present;
    CLOSE = close;
  }

  private SlipstreamMac() {
  }

  private static MethodHandle instance(MethodHandles.Lookup lookup, Class<?> owner, String name, Class<?> returnType) throws ReflectiveOperationException {
    return lookup.findVirtual(owner, name, MethodType.methodType(returnType)).asType(MethodType.methodType(returnType, Object.class));
  }

  public static boolean available() {
    return CREATE != null;
  }

  /** {@code vkCreateDevice}, additionally enabling {@code VK_EXT_metal_objects} when Slipstream can present with it. */
  public static int createDevice(final VkPhysicalDevice physical, final VkDeviceCreateInfo info, final PointerBuffer result) {
    if (CREATE_DEVICE != null) {
      try {
        return (int) CREATE_DEVICE.invokeExact(physical, info, (VkAllocationCallbacks) null, result);
      } catch (Throwable t) {
        LOGGER.warn("Slipstream device creation failed; creating the device without it", t);
      }
    }

    return VK12.vkCreateDevice(physical, info, null, result);
  }

  /** Creates a presenter for the window, or returns null if Slipstream can't present to it. */
  public static @Nullable Presenter create(final VkDevice device, final long window, final int width, final int height, final int format) {
    if (CREATE == null) {
      return null;
    }

    try {
      Object handle = (Object) CREATE.invokeExact(device, window, false, width, height, format);
      if (handle == null) {
        return null;
      }

      LOGGER.info("Presenting Vulkan through Slipstream IOSurfaces ({}x{}); MoltenVK swapchain acquire/present bypassed", width, height);
      return new Presenter(handle);
    } catch (Throwable t) {
      LOGGER.warn("Slipstream presenter initialization failed; keeping the MoltenVK swapchain", t);
      return null;
    }
  }

  private static RuntimeException rethrow(Throwable t) {
    if (t instanceof RuntimeException e) {
      return e;
    }
    if (t instanceof Error e) {
      throw e;
    }
    return new IllegalStateException(t);
  }

  public static final class Presenter {
    private final Object handle;

    private Presenter(final Object handle) {
      this.handle = handle;
    }

    /** Shows any finished frame, then reserves a free IOSurface. Returns false if none is free yet. */
    public boolean acquire() {
      try {
        return (boolean) ACQUIRE.invokeExact(this.handle);
      } catch (Throwable t) {
        throw rethrow(t);
      }
    }

    public long image() {
      try {
        return (long) IMAGE.invokeExact(this.handle);
      } catch (Throwable t) {
        throw rethrow(t);
      }
    }

    public long semaphore() {
      try {
        return (long) SEMAPHORE.invokeExact(this.handle);
      } catch (Throwable t) {
        throw rethrow(t);
      }
    }

    /** Timeline value the blit submission must signal for the reserved IOSurface. */
    public long submitted() {
      try {
        return (long) SUBMITTED.invokeExact(this.handle);
      } catch (Throwable t) {
        throw rethrow(t);
      }
    }

    /** Non-blocking: hands the newest completed IOSurface to the window's layer. */
    public void present() {
      try {
        PRESENT.invokeExact(this.handle);
      } catch (Throwable t) {
        throw rethrow(t);
      }
    }

    public void close() {
      try {
        CLOSE.invokeExact(this.handle);
      } catch (Throwable t) {
        throw rethrow(t);
      }
    }
  }
}
