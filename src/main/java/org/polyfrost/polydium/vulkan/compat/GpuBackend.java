package org.polyfrost.polydium.vulkan.compat;

//? if < 26.1 {
/*import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.systems.GpuDevice;
import org.jspecify.annotations.Nullable;

public interface GpuBackend {
  String getName();

  void setWindowHints();

  void handleWindowCreationErrors(final GLFWErrorCapture.@Nullable Error error) throws BackendCreationException;

  GpuDevice createDevice(long window, ShaderSource defaultShaderSource, GpuDebugOptions debugOptions);
}
*///?}
