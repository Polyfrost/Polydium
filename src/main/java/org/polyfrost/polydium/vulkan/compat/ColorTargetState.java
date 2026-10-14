package org.polyfrost.polydium.vulkan.compat;

//? if < 26.1 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import java.util.Optional;

public record ColorTargetState(Optional<BlendFunction> blendFunction, int writeMask) {
  public static final int WRITE_RED = 1;
  public static final int WRITE_GREEN = 2;
  public static final int WRITE_BLUE = 4;
  public static final int WRITE_ALPHA = 8;
  public static final int WRITE_COLOR = 7;
  public static final int WRITE_ALL = 15;
  public static final int WRITE_NONE = 0;
  public static final ColorTargetState DEFAULT = new ColorTargetState(Optional.empty(), 15);

  public ColorTargetState(final BlendFunction blendFunction) {
    this(Optional.of(blendFunction), 15);
  }

  public boolean writeRed() {
    return (this.writeMask & 1) != 0;
  }

  public boolean writeGreen() {
    return (this.writeMask & 2) != 0;
  }

  public boolean writeBlue() {
    return (this.writeMask & 4) != 0;
  }

  public boolean writeAlpha() {
    return (this.writeMask & 8) != 0;
  }
}
*///?}
