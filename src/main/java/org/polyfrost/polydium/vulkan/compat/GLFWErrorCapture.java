package org.polyfrost.polydium.vulkan.compat;

//? if < 26.1 {
/*import java.util.Locale;

public final class GLFWErrorCapture {
  public record Error(int error, String description) {
    @Override
    public String toString() {
      return String.format(Locale.ROOT, "[GLFW 0x%X] %s", this.error, this.description);
    }
  }
}
*///?}
