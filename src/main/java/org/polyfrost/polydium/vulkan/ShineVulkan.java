package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL20;
import org.polyfrost.polydium.compat.Mods;
import org.polyfrost.polydium.vulkan.compat.RenderArea;
import org.polyfrost.polydium.vulkan.compat.RenderPassDescriptor;

public final class ShineVulkan {
  public static final boolean LOADED = Mods.loaded("shine");

  private static final int PROGRAM = 1;
  private static final int MAX_LIGHTS = 64;
  private static final int COUNT = 0;
  private static final int POS_RADIUS = 1;
  private static final int COLOR = 2;
  private static final int BLOCK_SIZE = 16 + 2 * MAX_LIGHTS * 16;
  private static final int REGION_WIDTH = 128;
  private static final int REGION_HEIGHT = 64;
  private static final int REGION_LENGTH = 128;

  private static final Identifier TERRAIN_SHADER = Identifier.fromNamespaceAndPath("polydium", "sodium/block_layer_opaque");

  private static boolean useMasks;
  private static int lightCount;
  private static final float[] posRadius = new float[MAX_LIGHTS * 4];
  private static final float[] color = new float[MAX_LIGHTS * 4];
  private static boolean dirty;

  private static DynamicUniformStorage<Block> storage;
  private static GpuBufferSlice slice;
  private static final List<GpuBufferSlice> slices = new ArrayList<>();
  private static int nextSlice;

  private ShineVulkan() {
  }

  public static VulkanRenderPass createRenderPass(VulkanDevice device, RenderTarget target) {
    RenderTarget bloom = (RenderTarget) Shine.call(Shine.SOURCE_TARGET);
    RenderTarget rim = (RenderTarget) Shine.call(Shine.MASK_TARGET);
    GpuTextureView colorView = target.getColorTextureView();
    RenderPassDescriptor descriptor = RenderPassDescriptor.create(() -> "Terrain").withColorAttachment(colorView, Optional.empty());
    if (bloom != null) descriptor.withColorAttachment(bloom.getColorTextureView());
    else descriptor.withUnusedColorAttachment();
    if (rim != null) descriptor.withColorAttachment(rim.getColorTextureView());
    else descriptor.withUnusedColorAttachment();
    if (target.getDepthTextureView() != null) descriptor.withDepthAttachment(target.getDepthTextureView(), OptionalDouble.empty());
    descriptor.withRenderArea(new RenderArea(0, 0, colorView.getWidth(0), colorView.getHeight(0)));
    return (VulkanRenderPass) device.createCommandEncoder().createRenderPass(descriptor);
  }

  public static void beginPass() {
    useMasks = Shine.call(Shine.MASK_VIEW) != null && (boolean) Shine.call(Shine.HAS_CUSTOM_MASKS);
    Shine.run(Shine.SET_PROGRAM, PROGRAM);
    Shine.run(Shine.UPLOAD, PROGRAM);
    slices.clear();
    nextSlice = 0;
    dirty = true;
  }

  public static void prepareRegion(RenderRegion region) {
    int x = region.getOriginX();
    int y = region.getOriginY();
    int z = region.getOriginZ();
    try {
      if ((boolean) Shine.SHOULD_UPLOAD_REGION.invokeExact(PROGRAM, x, y, z, REGION_WIDTH, REGION_HEIGHT, REGION_LENGTH)) {
        Shine.UPLOAD_BOUNDS.invokeExact(PROGRAM, (double) x, (double) y, (double) z,
          (double) (x + REGION_WIDTH), (double) (y + REGION_HEIGHT), (double) (z + REGION_LENGTH));
      }
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }

    if (dirty) {
      if (storage == null) storage = new DynamicUniformStorage<>("Shine terrain UBO", BLOCK_SIZE, 64);
      slice = storage.writeUniform(new Block());
      dirty = false;
    }
    slices.add(slice);
  }

  public static void bindPass(VulkanRenderPass pass, GpuTextureView blockAtlas, GpuSampler blockSampler) {
    GpuTextureView mask = (GpuTextureView) Shine.call(Shine.MASK_VIEW);
    pass.bindTexture("u_ShineMaskTex", mask != null ? mask : blockAtlas,
      mask != null ? RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST) : blockSampler);
  }

  public static void bindRegion(VulkanRenderPass pass) {
    pass.setUniform("ShineTerrain", slices.get(nextSlice++));
  }

  public static void endPass() {
    Shine.run(Shine.SET_PROGRAM, 0);
  }

  public static void endFrame() {
    if (storage != null) storage.endFrame();
  }

  private static final class Block implements DynamicUniformStorage.DynamicUniform {
    @Override
    public void write(ByteBuffer buffer) {
      int base = buffer.position();
      buffer.putInt(base, useMasks ? 1 : 0);
      buffer.putInt(base + 4, lightCount);
      for (int i = 0; i < MAX_LIGHTS * 4; i++) {
        buffer.putFloat(base + 16 + i * 4, posRadius[i]);
        buffer.putFloat(base + 16 + MAX_LIGHTS * 16 + i * 4, color[i]);
      }
    }
  }

  public static int glGetUniformLocation(int program, CharSequence name) {
    if (!VulkanActive.active) return GL20.glGetUniformLocation(program, name);
    return switch (name.toString()) {
      case "u_ShineColoredLightCount" -> COUNT;
      case "u_ShineColoredLightPosRadius" -> POS_RADIUS;
      case "u_ShineColoredLightColor" -> COLOR;
      default -> -1;
    };
  }

  public static void glUniform1i(int location, int value) {
    if (!VulkanActive.active) {
      GL20.glUniform1i(location, value);
    } else if (location == COUNT) {
      lightCount = Math.min(value, MAX_LIGHTS);
      dirty = true;
    }
  }

  public static void glUniform4fv(int location, FloatBuffer value) {
    if (!VulkanActive.active) {
      GL20.glUniform4fv(location, value);
      return;
    }
    float[] target = location == POS_RADIUS ? posRadius : location == COLOR ? color : null;
    if (target != null) {
      value.get(value.position(), target, 0, Math.min(value.remaining(), target.length));
      dirty = true;
    }
  }

  public static Object attachmentGate(Object directStateAccess) {
    return VulkanActive.active ? Boolean.TRUE : directStateAccess;
  }

  static String shader(Identifier id, ShaderType type, String source) {
    if (!LOADED || source == null) return source;
    if (id.equals(VANILLA_TERRAIN_SHADER)) return vanillaTerrain(source);
    if (!id.equals(TERRAIN_SHADER)) return source;
    return type == ShaderType.VERTEX ? vertex(source) : fragment(source);
  }

  private static final Identifier VANILLA_TERRAIN_SHADER = Identifier.withDefaultNamespace("core/terrain");

  private static String vanillaTerrain(String source) {
    if (!source.contains("u_ShineColoredLightCount") && !source.contains("MaskSampler")) return source;
    return source
      .replace("uniform int u_ShineColoredLightCount;", "const int u_ShineColoredLightCount = 0;")
      .replace("uniform vec4 u_ShineColoredLightPosRadius[64];", "vec4 u_ShineColoredLightPosRadius[64];")
      .replace("uniform vec4 u_ShineColoredLightColor[64];", "vec4 u_ShineColoredLightColor[64];");
  }

  private static final String BLOCK = """
    layout(std140) uniform ShineTerrain {
        int u_ShineUseMasks;
        int u_ShineColoredLightCount;
        vec4 u_ShineColoredLightPosRadius[64];
        vec4 u_ShineColoredLightColor[64];
    };
    """;

  private static String vertex(String source) {
    String shader = source.replace("\r\n", "\n");
    shader = replace(shader, "out vec2 v_TexCoord;", "out vec2 v_TexCoord;\nflat out uint v_Material;\nflat out uint v_ShineRimLightIncluded;");
    shader = replace(shader, "uniform sampler2D u_LightTex; // The light map texture sampler", """
      uniform sampler2D u_LightTex; // The light map texture sampler
      """ + BLOCK + """

      vec3 shine_colored_light_tonemap(vec3 c) {
          float l = dot(c, vec3(0.2126, 0.7152, 0.0722));
          vec3 tc = c / (c + 1.0);
          return mix(c / (l + 1.0), tc, tc);
      }

      vec4 shine_apply_colored_light(vec3 position, vec4 baseColor, vec2 lightUv) {
          vec3 lightColor = vec3(0.0);
          float blockLight = smoothstep(0.5 / 16.0, 20.5 / 16.0, clamp(lightUv.x, 0.0, 1.0));
          if (u_ShineColoredLightCount <= 0 || blockLight <= 0.0) {
              return baseColor;
          }
          float lightScale = blockLight * 3.5;
          for (int i = 0; i < u_ShineColoredLightCount; ++i) {
              vec4 posRadius = u_ShineColoredLightPosRadius[i];
              vec4 colorIntensity = u_ShineColoredLightColor[i];
              vec3 delta = posRadius.xyz - position;
              float distSqr = dot(delta, delta);
              float radiusSqr = posRadius.w * posRadius.w;
              if (distSqr >= radiusSqr) {
                  continue;
              }
              float intensity = smoothstep(0.0, 1.0, 1.0 - sqrt(distSqr) * colorIntensity.a);
              lightColor = max(lightColor, colorIntensity.rgb * intensity * lightScale);
          }
          if (dot(lightColor, vec3(1.0)) <= 0.0) {
              return baseColor;
          }
          return vec4(baseColor.rgb + clamp(shine_colored_light_tonemap(lightColor), 0.0, 1.0), baseColor.a);
      }
      """);
    return replace(shader, "v_Color = _vert_color * texture(u_LightTex, _vert_tex_light_coord);", """
          uint shineLightX = uint(round(_vert_tex_light_coord.x * 256.0));
          uint shineLightY = uint(round(_vert_tex_light_coord.y * 256.0));
          v_ShineRimLightIncluded = shineLightX & 1u;
          vec2 shineLightCoord = vec2(float(shineLightX & 254u), float(shineLightY)) / vec2(256.0);
          v_Color = _vert_color * texture(u_LightTex, shineLightCoord);
          v_Color = shine_apply_colored_light(position, v_Color, shineLightCoord);
          v_Material = _material_params;
      """);
  }

  private static String fragment(String source) {
    String shader = source.replace("\r\n", "\n");
    shader = replace(shader, "uniform sampler2D u_BlockTex; // The block texture", """
      uniform sampler2D u_BlockTex; // The block texture
      uniform sampler2D u_ShineMaskTex; // Shine runtime mask atlas
      """ + BLOCK + """

      vec2 shine_nearest_uv(vec2 uv, vec2 pixelSize) {
          vec2 du = dFdx(uv);
          vec2 dv = dFdy(uv);
          vec2 texelScreenSize = sqrt(du * du + dv * dv);
          vec2 safeTexelScreenSize = max(texelScreenSize, vec2(1.0e-6));
          vec2 uvTexelCoords = uv / pixelSize;
          vec2 texelCenter = round(uvTexelCoords) - 0.5f;
          vec2 texelOffset = uvTexelCoords - texelCenter;
          texelOffset = (texelOffset - 0.5f) * pixelSize / safeTexelScreenSize + 0.5f;
          texelOffset = clamp(texelOffset, 0.0f, 1.0f);
          return (texelCenter + texelOffset) * pixelSize;
      }

      float shine_sample_mask(vec2 uv) {
          ivec2 maskSize = textureSize(u_ShineMaskTex, 0);
          ivec2 texel = ivec2(floor(uv * vec2(maskSize)));
          texel = clamp(texel, ivec2(0), maskSize - ivec2(1));
          return texelFetch(u_ShineMaskTex, texel, 0).r;
      }

      vec4 shine_sample_base_texel(sampler2D source, vec2 uv) {
          ivec2 atlasSize = textureSize(source, 0);
          ivec2 texel = ivec2(floor(uv * vec2(atlasSize)));
          texel = clamp(texel, ivec2(0), atlasSize - ivec2(1));
          return texelFetch(source, texel, 0);
      }
      """);
    shader = replace(shader, "out vec4 fragColor; // The output fragment for the color framebuffer", """
      layout(location = 0) out vec4 fragColor; // The output fragment for the color framebuffer
      layout(location = 1) out vec4 bloomColor;
      layout(location = 2) out vec4 shineRimMaskColor;""");
    shader = replace(shader, "in float fadeFactor;", "in float fadeFactor;\nflat in uint v_Material;\nflat in uint v_ShineRimLightIncluded;");
    shader = replace(shader, "void main() {", """
      float shine_decode_source_strength(uint packedMaterial) {
          uint sourceCode = (packedMaterial >> 3u) & 0x1Fu;
          if (sourceCode <= 20u) {
              return float(sourceCode) * 0.05;
          }
          return 1.0 + float(sourceCode - 20u) * (4.0 / 11.0);
      }

      float shine_decode_rim_mask(uint packedMaterial) {
          return ((packedMaterial >> 8u) & 1u) == 1u ? 1.0 : 0.0;
      }

      void main() {""");
    return replace(shader, "fragColor = _linearFog(color, v_FragDistance, u_FogColor, u_EnvironmentFog, u_RenderFog, fadeFactor);\n}", """
      fragColor = _linearFog(color, v_FragDistance, u_FogColor, u_EnvironmentFog, u_RenderFog, fadeFactor);
          shineRimMaskColor = vec4(float(v_ShineRimLightIncluded));
          float maskValue = (u_ShineUseMasks == 0) ? 1.0 : shine_sample_mask(shine_nearest_uv(v_TexCoord, u_TexelSize));
          float bloomStrength = shine_decode_source_strength(v_Material);
          if (maskValue <= 0.5 || bloomStrength <= 1.0e-5) {
              bloomColor = vec4(0.0);
          } else {
              float fogValue = max(1.0 - fadeFactor, total_fog_value(v_FragDistance.y, v_FragDistance.x, u_EnvironmentFog.x, u_EnvironmentFog.y, u_RenderFog.x, u_RenderFog.y));
              float fogAttenuation = 1.0 - fogValue;
              vec4 shineSource = shine_sample_base_texel(u_BlockTex, shine_nearest_uv(v_TexCoord, u_TexelSize)) * v_Color;
              vec4 shineFoggedSource = _linearFog(shineSource, v_FragDistance, u_FogColor, u_EnvironmentFog, u_RenderFog, fadeFactor);
              bloomColor = vec4(shineFoggedSource.rgb * shineFoggedSource.a * fogAttenuation, clamp((bloomStrength * maskValue) / 5.0, 0.0, 1.0));
          }
      }
      """);
  }

  private static String replace(String shader, String target, String replacement) {
    if (!shader.contains(target)) throw new IllegalStateException("Shine Vulkan terrain patch: missing '" + target + "'");
    return shader.replace(target, replacement);
  }

  private static final class Shine {
    static final MethodHandle SOURCE_TARGET;
    static final MethodHandle MASK_TARGET;
    static final MethodHandle MASK_VIEW;
    static final MethodHandle HAS_CUSTOM_MASKS;
    static final MethodHandle SET_PROGRAM;
    static final MethodHandle UPLOAD;
    static final MethodHandle SHOULD_UPLOAD_REGION;
    static final MethodHandle UPLOAD_BOUNDS;

    static {
      try {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        SOURCE_TARGET = getter(lookup, "com.bloom.client.render.BloomSourceRenderer", "getSourceTarget", RenderTarget.class);
        MASK_TARGET = getter(lookup, "com.bloom.client.rimlight.render.RimLightMaskRenderer", "getMaskTarget", RenderTarget.class);
        MASK_VIEW = getter(lookup, "com.bloom.client.render.BloomMaskAtlas", "getMaskTextureView", GpuTextureView.class);
        HAS_CUSTOM_MASKS = getter(lookup, "com.bloom.client.config.BloomMaskConfig", "hasCustomMasks", boolean.class);
        Class<?> lights = Class.forName("com.bloom.client.coloredlight.ColoredLightRenderer");
        SET_PROGRAM = lookup.findStatic(lights, "setCurrentSodiumProgramId", MethodType.methodType(void.class, int.class));
        UPLOAD = lookup.findStatic(lights, "uploadSodiumUniforms", MethodType.methodType(void.class, int.class));
        SHOULD_UPLOAD_REGION = lookup.findStatic(lights, "shouldUploadSodiumUniformsForRegion",
          MethodType.methodType(boolean.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class));
        UPLOAD_BOUNDS = lookup.findStatic(lights, "uploadSodiumUniformsForBounds",
          MethodType.methodType(void.class, int.class, double.class, double.class, double.class, double.class, double.class, double.class));
      } catch (ReflectiveOperationException e) {
        throw new ExceptionInInitializerError(e);
      }
    }

    private static MethodHandle getter(MethodHandles.Lookup lookup, String owner, String name, Class<?> type) throws ReflectiveOperationException {
      return lookup.findStatic(Class.forName(owner), name, MethodType.methodType(type)).asType(MethodType.methodType(Object.class));
    }

    static Object call(MethodHandle handle) {
      try {
        return handle.invokeExact();
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }

    static void run(MethodHandle handle, int value) {
      try {
        handle.invokeExact(value);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }
}
