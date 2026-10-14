package org.polyfrost.polydium.vulkan;

import org.polyfrost.polydium.vulkan.compat.GpuFormat;
import org.polyfrost.polydium.vulkan.compat.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import org.polyfrost.polydium.vulkan.compat.BlendFactor;
import org.polyfrost.polydium.vulkan.compat.BlendOp;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.PolygonMode;
import org.polyfrost.polydium.vulkan.compat.GpuSurface;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;

public final class VulkanConst {
  public VulkanConst() {
  }

  public static int textureUsageToVk(final @GpuTexture.Usage int usage, final GpuFormat format) {
    int vkUsage = 0;
    if ((usage & 8) != 0) {
      vkUsage |= 2;
      if (format.hasColorAspect()) {
        vkUsage |= 16;
      }

      if (format.hasDepthAspect()) {
        vkUsage |= 32;
      }
    }

    if ((usage & 4) != 0) {
      vkUsage |= 4;
    }

    if ((usage & 1) != 0) {
      vkUsage |= 2;
    }

    if ((usage & 2) != 0) {
      vkUsage |= 1;
    }

    return vkUsage;
  }

  public static int bufferUsageToVk(final @GpuBuffer.Usage int usage) {
    int result = 0;
    if ((usage & 8) != 0) {
      result |= 2;
    }

    if ((usage & 16) != 0) {
      result |= 1;
    }

    if ((usage & 32) != 0) {
      result |= 128;
    }

    if ((usage & 64) != 0) {
      result |= 64;
    }

    if ((usage & 128) != 0) {
      result |= 16;
    }

    if ((usage & 256) != 0) {
      result |= 4;
    }

    if ((usage & 512) != 0) {
      result |= 256;
    }

    return result;
  }

  public static int formatAspectMask(final GpuFormat format) {
    int aspectMask = 0;
    if (format.hasColorAspect()) {
      aspectMask |= 1;
    }

    if (format.hasDepthAspect()) {
      aspectMask |= 2;
    }

    if (format.hasStencilAspect()) {
      aspectMask |= 4;
    }

    return aspectMask;
  }

  public static int toVk(final AddressMode addressMode) {
    return switch (addressMode) {
      case REPEAT -> 0;
      case CLAMP_TO_EDGE -> 2;
    };
  }

  public static int toVk(final FilterMode filter) {
    return switch (filter) {
      case NEAREST -> 0;
      case LINEAR -> 1;
    };
  }

  private static final com.mojang.blaze3d.textures.TextureFormat FLASHBACK_R32_FLOAT = java.util.Arrays.stream(com.mojang.blaze3d.textures.TextureFormat.values())
    .filter(format -> format.name().equals("FLASHBACK_R32_FLOAT")).findFirst().orElse(null);

  public static GpuFormat toGpuFormat(final com.mojang.blaze3d.textures.TextureFormat format) {
    return switch (format) {
      case RGBA8 -> GpuFormat.RGBA8_UNORM;
      case RED8 -> GpuFormat.R8_UNORM;
      case RED8I -> GpuFormat.R8_SINT;
      case DEPTH32 -> GpuFormat.D32_FLOAT;
      default -> {
        if (format == FLASHBACK_R32_FLOAT) yield GpuFormat.R32_FLOAT;
        throw new IllegalArgumentException("Unsupported texture format " + format);
      }
    };
  }

  public static com.mojang.blaze3d.textures.TextureFormat toTextureFormat(final GpuFormat format) {
    return switch (format) {
      case R8_UNORM -> com.mojang.blaze3d.textures.TextureFormat.RED8;
      case R8_SINT -> com.mojang.blaze3d.textures.TextureFormat.RED8I;
      case D32_FLOAT -> com.mojang.blaze3d.textures.TextureFormat.DEPTH32;
      case R32_FLOAT -> FLASHBACK_R32_FLOAT != null ? FLASHBACK_R32_FLOAT : com.mojang.blaze3d.textures.TextureFormat.RGBA8;
      default -> com.mojang.blaze3d.textures.TextureFormat.RGBA8;
    };
  }

  public static int toVk(final GpuFormat format) {
    return switch (format) {
      case R8_UNORM -> 9;
      case R8_SNORM -> 10;
      case RG8_UNORM -> 16;
      case RG8_SNORM -> 17;
      case RGB8_UNORM -> 23;
      case RGB8_SNORM -> 24;
      case RGBA8_UNORM -> 37;
      case RGBA8_SNORM -> 38;
      case R16_UNORM -> 70;
      case R16_SNORM -> 71;
      case RG16_UNORM -> 77;
      case RG16_SNORM -> 78;
      case RGB16_UNORM -> 84;
      case RGB16_SNORM -> 85;
      case RGBA16_UNORM -> 91;
      case RGBA16_SNORM -> 92;
      case R8_UINT -> 13;
      case R8_SINT -> 14;
      case RG8_UINT -> 20;
      case RG8_SINT -> 21;
      case RGB8_UINT -> 27;
      case RGB8_SINT -> 28;
      case RGBA8_UINT -> 41;
      case RGBA8_SINT -> 42;
      case R16_UINT -> 74;
      case R16_SINT -> 75;
      case RG16_UINT -> 81;
      case RG16_SINT -> 82;
      case RGB16_UINT -> 88;
      case RGB16_SINT -> 89;
      case RGBA16_UINT -> 95;
      case RGBA16_SINT -> 96;
      case R32_UINT -> 98;
      case R32_SINT -> 99;
      case RG32_UINT -> 101;
      case RG32_SINT -> 102;
      case RGB32_UINT -> 104;
      case RGB32_SINT -> 105;
      case RGBA32_UINT -> 107;
      case RGBA32_SINT -> 108;
      case R16_FLOAT -> 76;
      case RG16_FLOAT -> 83;
      case RGB16_FLOAT -> 90;
      case RGBA16_FLOAT -> 97;
      case R32_FLOAT -> 100;
      case RG32_FLOAT -> 103;
      case RGB32_FLOAT -> 106;
      case RGBA32_FLOAT -> 109;
      case RGB10A2_UNORM -> 64;
      case RGB10A2_UINT -> 68;
      case RG11B10_FLOAT -> 122;
      case D32_FLOAT -> 126;
      case D32_FLOAT_S8_UINT -> 130;
      case D24_UNORM_S8_UINT -> 129;
      case D16_UNORM -> 124;
      case S8_UINT -> 127;
    };
  }

  public static int toVk(final BlendFactor factor) {
    return switch (factor) {
      case CONSTANT_ALPHA -> 12;
      case CONSTANT_COLOR -> 10;
      case ONE_MINUS_CONSTANT_ALPHA -> 13;
      case ONE_MINUS_CONSTANT_COLOR -> 11;
      case DST_ALPHA -> 8;
      case DST_COLOR -> 4;
      case ONE -> 1;
      case ONE_MINUS_DST_ALPHA -> 9;
      case ONE_MINUS_DST_COLOR -> 5;
      case ONE_MINUS_SRC_ALPHA -> 7;
      case ONE_MINUS_SRC_COLOR -> 3;
      case SRC_ALPHA -> 6;
      case SRC_ALPHA_SATURATE -> 14;
      case SRC_COLOR -> 2;
      case ZERO -> 0;
    };
  }

  public static int toVk(final com.mojang.blaze3d.vertex.VertexFormatElement element, final int shaderType) {
    int n = element.count() - 1;
    //? if >= 26.1 {
    boolean norm = element.normalized();
    //?} else {
    /*boolean norm = element.usage() == com.mojang.blaze3d.vertex.VertexFormatElement.Usage.NORMAL
      || element.usage() == com.mojang.blaze3d.vertex.VertexFormatElement.Usage.COLOR;
    *///?}
    boolean unsignedIn = shaderType == org.lwjgl.util.spvc.Spvc.SPVC_BASETYPE_UINT8 || shaderType == org.lwjgl.util.spvc.Spvc.SPVC_BASETYPE_UINT16
      || shaderType == org.lwjgl.util.spvc.Spvc.SPVC_BASETYPE_UINT32;
    boolean signedIn = shaderType == org.lwjgl.util.spvc.Spvc.SPVC_BASETYPE_INT8 || shaderType == org.lwjgl.util.spvc.Spvc.SPVC_BASETYPE_INT16
      || shaderType == org.lwjgl.util.spvc.Spvc.SPVC_BASETYPE_INT32;
    if (!norm && (unsignedIn || signedIn) && element.type() != com.mojang.blaze3d.vertex.VertexFormatElement.Type.FLOAT) {
      return (switch (element.type().size()) {
        case 1 -> unsignedIn ? new int[]{13, 20, 27, 41} : new int[]{14, 21, 28, 42};
        case 2 -> unsignedIn ? new int[]{74, 81, 88, 95} : new int[]{75, 82, 89, 96};
        default -> unsignedIn ? new int[]{98, 101, 104, 107} : new int[]{99, 102, 105, 108};
      })[n];
    }
    return switch (element.type()) {
      case FLOAT -> new int[]{100, 103, 106, 109}[n];
      case UBYTE -> (norm ? new int[]{9, 16, 23, 37} : new int[]{13, 20, 27, 41})[n];
      case BYTE -> (norm ? new int[]{10, 17, 24, 38} : new int[]{14, 21, 28, 42})[n];
      case USHORT -> (norm ? new int[]{70, 77, 84, 91} : new int[]{74, 81, 88, 95})[n];
      case SHORT -> (norm ? new int[]{71, 78, 85, 92} : new int[]{75, 82, 89, 96})[n];
      case UINT -> new int[]{98, 101, 104, 107}[n];
      case INT -> new int[]{99, 102, 105, 108}[n];
    };
  }

  public static int toVk(final BlendOp blendOp) {
    return switch (blendOp) {
      case ADD -> 0;
      case SUBTRACT -> 1;
      case REVERSE_SUBTRACT -> 2;
      case MIN -> 3;
      case MAX -> 4;
    };
  }

  public static int toVk(final CompareOp op) {
    return switch (op) {
      case ALWAYS_PASS -> 7;
      case LESS_THAN -> 1;
      case LESS_THAN_OR_EQUAL -> 3;
      case EQUAL -> 2;
      case NOT_EQUAL -> 5;
      case GREATER_THAN_OR_EQUAL -> 6;
      case GREATER_THAN -> 4;
      case NEVER_PASS -> 0;
    };
  }

  public static int toVk(final PolygonMode polygonMode) {
    return switch (polygonMode) {
      case FILL -> 0;
      case WIREFRAME -> 1;
    };
  }

  public static int toVk(final com.mojang.blaze3d.vertex.VertexFormat.Mode primitiveTopology) {
    return switch (primitiveTopology) {
      case LINES -> 3;
      case DEBUG_LINES -> 1;
      case DEBUG_LINE_STRIP -> 2;
      case POINTS -> 0;
      case TRIANGLES -> 3;
      case TRIANGLE_STRIP -> 4;
      case TRIANGLE_FAN -> 5;
      case QUADS -> 3;
    };
  }

  public static int toVk(final ColorTargetState colorTargetState) {
    int result = 0;
    if (colorTargetState.writeAlpha()) {
      result |= 8;
    }

    if (colorTargetState.writeRed()) {
      result |= 1;
    }

    if (colorTargetState.writeGreen()) {
      result |= 2;
    }

    if (colorTargetState.writeBlue()) {
      result |= 4;
    }

    return result;
  }

  public static int toVk(final GpuSurface.PresentMode mode) {
    return switch (mode) {
      case IMMEDIATE -> 0;
      case MAILBOX -> 1;
      case FIFO -> 2;
      case FIFO_RELAXED -> 3;
    };
  }

  public static com.mojang.blaze3d.vertex.VertexFormatElement vertexElement(com.mojang.blaze3d.vertex.VertexFormatElement.Type type, int count) {
    for (int id = 0; id < com.mojang.blaze3d.vertex.VertexFormatElement.MAX_COUNT; id++) {
      if (com.mojang.blaze3d.vertex.VertexFormatElement.byId(id) == null) {
        //? if >= 26.1 {
        return com.mojang.blaze3d.vertex.VertexFormatElement.register(id, 0, type, false, count);
        //?} else {
        /*return com.mojang.blaze3d.vertex.VertexFormatElement.register(id, 0, type, com.mojang.blaze3d.vertex.VertexFormatElement.Usage.GENERIC, count);
        *///?}
      }
    }
    throw new IllegalStateException("No free vertex format element id");
  }
}
