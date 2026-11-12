package org.polyfrost.polydium.vulkan;

import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL33C;
import org.polyfrost.polydium.vulkan.compat.GpuQueryPool;

import java.util.OptionalLong;

// stands in for the GL timestamp queries Reflex AntiLag times GPU frames with
public final class ReflexVulkan {
  // Reflex keeps at most ~10 frames of queries alive, so a ring this size never reuses a slot that is still pending
  private static final int QUERIES = 256;
  private static final long RECALIBRATE_NS = 1_000_000_000L;

  private static GpuQueryPool pool;
  private static int nextQuery;
  private static long calibratedHostNs;
  private static long calibratedGpuNs;

  private ReflexVulkan() {
  }

  public static int glGenQueries() {
    if (!VulkanActive.active) return GL32C.glGenQueries();
    if (pool == null) pool = VulkanDevice.active.createTimestampQueryPool(QUERIES);
    int slot = nextQuery;
    nextQuery = (nextQuery + 1) % QUERIES;
    return slot + 1;
  }

  public static void glDeleteQueries(int id) {
    if (!VulkanActive.active) GL32C.glDeleteQueries(id);
  }

  public static void glQueryCounter(int id, int target) {
    if (!VulkanActive.active) GL33C.glQueryCounter(id, target);
    else VulkanDevice.active.createCommandEncoder().writeTimestamp(pool, id - 1);
  }

  public static long glGetQueryObjecti64(int id, int pname) {
    if (!VulkanActive.active) return GL33C.glGetQueryObjecti64(id, pname);
    OptionalLong value = pool.getValue(id - 1);
    if (pname == GL33C.GL_QUERY_RESULT_AVAILABLE) return value.isPresent() ? GL33C.GL_TRUE : GL33C.GL_FALSE;
    // Reflex reads this only after the result is available; 0 makes it fall back to its CPU-side anchor
    return value.isPresent() ? toNs(value.getAsLong()) : 0L;
  }

  public static void glGetInteger64v(int pname, long[] data) {
    if (!VulkanActive.active) {
      GL33C.glGetInteger64v(pname, data);
      return;
    }
    // Reflex reads this every frame, but the Vulkan clock can only be sampled with a blocking submit
    long now = System.nanoTime();
    if (calibratedHostNs == 0 || now - calibratedHostNs >= RECALIBRATE_NS) {
      calibratedGpuNs = toNs(VulkanDevice.active.getTimestampNow());
      calibratedHostNs = System.nanoTime();
    }
    data[0] = calibratedGpuNs + (now - calibratedHostNs);
  }

  public static void glGetQueryiv(int target, int pname, int[] params) {
    if (!VulkanActive.active) GL33C.glGetQueryiv(target, pname, params);
    else params[0] = 64;
  }

  public static void glFinish() {
    if (!VulkanActive.active) GL33C.glFinish();
  }

  private static long toNs(long ticks) {
    return Math.round(ticks * (double) VulkanDevice.active.getDeviceInfo().timestampPeriod());
  }
}
