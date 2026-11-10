package org.polyfrost.polydium.vulkan.compat;

public class GpuDeviceLossException extends RuntimeException {
  public GpuDeviceLossException(final String message) {
    super(message);
  }
}
