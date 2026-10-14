package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryUtil;

public final class StellaVulkan {
  private static final RenderPipeline.Snippet SNIPPET = VulkanRenderPipeline.withDepth(RenderPipeline.builder()
    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
    .withCull(false), null)
    .buildSnippet();
  private static final RenderPipeline SHAPE = shape("shape", BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA);
  private static final RenderPipeline SHAPE_IN = shape("shape_in", new BlendFunction(SourceFactor.DST_ALPHA, DestFactor.ZERO));
  private static final RenderPipeline TEXTURE = VulkanRenderPipeline.withColor(RenderPipeline.builder(SNIPPET)
    .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/stella_lumina_texture"))
    .withVertexShader("core/position_tex_color")
    .withFragmentShader("core/position_tex_color")
    .withSampler("Sampler0")
    .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLES), new ColorTargetState(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA))
    .build();

  private static final class Texture {
    GpuTexture texture;
    GpuTextureView view;
    boolean mipmapped;
  }

  private static final Map<Integer, Texture> TEXTURES = new HashMap<>();
  private static final Map<Integer, Integer> STRIDES = new HashMap<>();
  private static final List<GpuBuffer> FRAME_BUFFERS = new ArrayList<>();
  private static MethodHandle renderContent;
  private static MethodHandle invoke;
  private static int nextId = 1;
  private static GpuTextureView target;
  private static GpuBufferSlice transforms;
  private static RenderPass pass;
  private static GpuBuffer vertices;
  private static int boundVao;
  private static int boundTexture;
  private static boolean maskBlend;
  private static boolean scissor;
  private static int scissorX, scissorY, scissorW, scissorH;

  private StellaVulkan() {
  }

  private static RenderPipeline shape(String name, BlendFunction blend) {
    return VulkanRenderPipeline.withColor(RenderPipeline.builder(SNIPPET)
      .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/stella_lumina_" + name))
      .withVertexShader("core/gui")
      .withFragmentShader("core/gui")
      .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES), new ColorTargetState(blend))
      .build();
  }

  public static void renderToTexture(Object state) throws Throwable {
    GpuTextureView color = RenderSystem.outputColorTextureOverride;
    if (color == null) return;
    if (renderContent == null) {
      Class<?> function2 = Class.forName("kotlin.jvm.functions.Function2", false, state.getClass().getClassLoader());
      renderContent = MethodHandles.publicLookup().findVirtual(state.getClass(), "getRenderContent", MethodType.methodType(function2))
        .asType(MethodType.methodType(Object.class, Object.class));
      invoke = MethodHandles.publicLookup().findVirtual(function2, "invoke", MethodType.methodType(Object.class, Object.class, Object.class))
        .asType(MethodType.methodType(Object.class, Object.class, Object.class, Object.class));
    }

    target = color;
    transforms = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
    try {
      Object content = renderContent.invokeExact(state);
      Object unused = invoke.invokeExact(content, (Object) color.getWidth(0), (Object) color.getHeight(0));
    } finally {
      endPass();
      target = null;
      vertices = null;
      FRAME_BUFFERS.forEach(GpuBuffer::close);
      FRAME_BUFFERS.clear();
    }
  }

  private static RenderPass pass() {
    if (pass == null) {
      pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Stella Lumina", target, OptionalInt.empty());
      RenderSystem.bindDefaultUniforms(pass);
      pass.setUniform("DynamicTransforms", transforms);
    }
    return pass;
  }

  private static void endPass() {
    if (pass != null) {
      pass.close();
      pass = null;
    }
  }

  public static void glDrawArrays(int mode, int first, int count) {
    if (!VulkanActive.active) {
      GL33C.glDrawArrays(mode, first, count);
      return;
    }
    if (target == null || vertices == null) return;
    boolean textured = STRIDES.getOrDefault(boundVao, 0) == 32;
    RenderPass pass = pass();
    pass.setPipeline(textured ? TEXTURE : maskBlend ? SHAPE_IN : SHAPE);
    pass.setVertexBuffer(0, vertices);
    if (scissor) pass.enableScissor(scissorX, scissorY, scissorW, scissorH);
    else pass.disableScissor();
    if (textured) {
      Texture texture = TEXTURES.get(boundTexture);
      if (texture == null) return;
      pass.bindTexture("Sampler0", texture.view, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR, texture.mipmapped));
    }
    pass.draw(first, count);
  }

  public static void nglBufferSubData(int target, long offset, long size, long data) {
    if (!VulkanActive.active) {
      GL33C.nglBufferSubData(target, offset, size, data);
      return;
    }
    int stride = STRIDES.getOrDefault(boundVao, 0);
    if (stride != 28 && stride != 32 || size < stride) return;
    boolean textured = stride == 32;
    VertexFormat format = textured ? DefaultVertexFormat.POSITION_TEX_COLOR : DefaultVertexFormat.POSITION_COLOR;
    int count = (int) (size / stride);
    try (ByteBufferBuilder bytes = new ByteBufferBuilder(count * format.getVertexSize())) {
      BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.TRIANGLES, format);
      for (int i = 0; i < count; i++) {
        long v = data + offset + (long) i * stride;
        float x = MemoryUtil.memGetFloat(v), y = MemoryUtil.memGetFloat(v + 4);
        if (textured) {
          builder.addVertex(x, y, 0.0F).setUv(MemoryUtil.memGetFloat(v + 8), MemoryUtil.memGetFloat(v + 12))
            .setColor(MemoryUtil.memGetFloat(v + 16), MemoryUtil.memGetFloat(v + 20), MemoryUtil.memGetFloat(v + 24), MemoryUtil.memGetFloat(v + 28));
        } else {
          float coverage = MemoryUtil.memGetFloat(v + 24);
          builder.addVertex(x, y, 0.0F).setColor(MemoryUtil.memGetFloat(v + 8) * coverage, MemoryUtil.memGetFloat(v + 12) * coverage,
            MemoryUtil.memGetFloat(v + 16) * coverage, MemoryUtil.memGetFloat(v + 20) * coverage);
        }
      }
      try (MeshData mesh = builder.buildOrThrow()) {
        vertices = RenderSystem.getDevice().createBuffer(() -> "Stella Lumina vertices", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
        FRAME_BUFFERS.add(vertices);
      }
    }
  }

  public static void glTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
    if (!VulkanActive.active) {
      GL33C.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
      return;
    }
    endPass();
    Texture texture = TEXTURES.get(boundTexture);
    if (texture == null || level != 0) return;
    close(texture);
    GpuDevice device = RenderSystem.getDevice();
    int mips = 32 - Integer.numberOfLeadingZeros(Math.max(width, height));
    texture.texture = device.createTexture(() -> "Stella Lumina " + boundTexture, GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
      TextureFormat.RGBA8, width, height, 1, mips);
    texture.view = device.createTextureView(texture.texture);
    texture.mipmapped = false;
    if (pixels == null) return;

    ByteBuffer rgba = pixels;
    if (format == 6403) {
      rgba = MemoryUtil.memAlloc(width * height * 4);
      for (int i = 0; i < width * height; i++) {
        byte coverage = pixels.get(pixels.position() + i);
        rgba.put(i * 4, coverage).put(i * 4 + 1, coverage).put(i * 4 + 2, coverage).put(i * 4 + 3, coverage);
      }
    }
    try {
      device.createCommandEncoder().writeToTexture(texture.texture, rgba, NativeImage.Format.RGBA, 0, 0, 0, 0, width, height);
    } finally {
      if (rgba != pixels) MemoryUtil.memFree(rgba);
    }
  }

  public static void glGenerateMipmap(int target) {
    if (!VulkanActive.active) {
      GL33C.glGenerateMipmap(target);
      return;
    }
    Texture texture = TEXTURES.get(boundTexture);
    if (texture == null || texture.texture == null) return;
    endPass();
    XaeroVulkan.generateMipmaps(texture.texture);
    texture.mipmapped = true;
  }

  public static int glGenTextures() {
    if (!VulkanActive.active) return GL33C.glGenTextures();
    int id = nextId++;
    TEXTURES.put(id, new Texture());
    return id;
  }

  public static void glDeleteTextures(int texture) {
    if (!VulkanActive.active) {
      GL33C.glDeleteTextures(texture);
      return;
    }
    Texture removed = TEXTURES.remove(texture);
    if (removed != null) close(removed);
  }

  private static void close(Texture texture) {
    if (texture.view != null) texture.view.close();
    if (texture.texture != null) texture.texture.close();
    texture.view = null;
    texture.texture = null;
  }

  public static void glBindTexture(int target, int texture) {
    if (!VulkanActive.active) GL33C.glBindTexture(target, texture);
    else boundTexture = texture;
  }

  public static void glBindVertexArray(int array) {
    if (!VulkanActive.active) GL33C.glBindVertexArray(array);
    else boundVao = array;
  }

  public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) {
    if (!VulkanActive.active) GL33C.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    else STRIDES.put(boundVao, stride);
  }

  public static void glBlendFunc(int sfactor, int dfactor) {
    if (!VulkanActive.active) GL33C.glBlendFunc(sfactor, dfactor);
    else maskBlend = sfactor == 772;
  }

  public static void glEnable(int cap) {
    if (!VulkanActive.active) GL33C.glEnable(cap);
    else if (cap == 3089) scissor = true;
  }

  public static void glDisable(int cap) {
    if (!VulkanActive.active) GL33C.glDisable(cap);
    else if (cap == 3089) scissor = false;
  }

  public static void glScissor(int x, int y, int width, int height) {
    if (!VulkanActive.active) {
      GL33C.glScissor(x, y, width, height);
      return;
    }
    scissorX = x;
    scissorY = y;
    scissorW = width;
    scissorH = height;
  }

  public static int glGenBuffers() {
    return VulkanActive.active ? nextId++ : GL33C.glGenBuffers();
  }

  public static int glGenVertexArrays() {
    return VulkanActive.active ? nextId++ : GL33C.glGenVertexArrays();
  }

  public static int glCreateProgram() {
    return VulkanActive.active ? nextId++ : GL33C.glCreateProgram();
  }

  public static int glCreateShader(int type) {
    return VulkanActive.active ? nextId++ : GL33C.glCreateShader(type);
  }

  public static int glGetProgrami(int program, int pname) {
    return VulkanActive.active ? 1 : GL33C.glGetProgrami(program, pname);
  }

  public static int glGetShaderi(int shader, int pname) {
    return VulkanActive.active ? 1 : GL33C.glGetShaderi(shader, pname);
  }

  public static String glGetProgramInfoLog(int program) {
    return VulkanActive.active ? "" : GL33C.glGetProgramInfoLog(program);
  }

  public static String glGetShaderInfoLog(int shader) {
    return VulkanActive.active ? "" : GL33C.glGetShaderInfoLog(shader);
  }

  public static int glGetUniformLocation(int program, CharSequence name) {
    return VulkanActive.active ? 0 : GL33C.glGetUniformLocation(program, name);
  }

  public static int glGetInteger(int pname) {
    return VulkanActive.active ? 0 : GL33C.glGetInteger(pname);
  }

  public static boolean glIsEnabled(int cap) {
    return !VulkanActive.active && GL33C.glIsEnabled(cap);
  }

  public static void glGetIntegerv(int pname, int[] params) {
    if (!VulkanActive.active) GL33C.glGetIntegerv(pname, params);
  }

  public static void glActiveTexture(int texture) {
    if (!VulkanActive.active) GL33C.glActiveTexture(texture);
  }

  public static void glAttachShader(int program, int shader) {
    if (!VulkanActive.active) GL33C.glAttachShader(program, shader);
  }

  public static void glBindBuffer(int target, int buffer) {
    if (!VulkanActive.active) GL33C.glBindBuffer(target, buffer);
  }

  public static void glBindSampler(int unit, int sampler) {
    if (!VulkanActive.active) GL33C.glBindSampler(unit, sampler);
  }

  public static void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
    if (!VulkanActive.active) GL33C.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
  }

  public static void glBufferData(int target, long size, int usage) {
    if (!VulkanActive.active) GL33C.glBufferData(target, size, usage);
  }

  public static void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
    if (!VulkanActive.active) GL33C.glColorMask(red, green, blue, alpha);
  }

  public static void glCompileShader(int shader) {
    if (!VulkanActive.active) GL33C.glCompileShader(shader);
  }

  public static void glDeleteBuffers(int buffer) {
    if (!VulkanActive.active) GL33C.glDeleteBuffers(buffer);
  }

  public static void glDeleteProgram(int program) {
    if (!VulkanActive.active) GL33C.glDeleteProgram(program);
  }

  public static void glDeleteShader(int shader) {
    if (!VulkanActive.active) GL33C.glDeleteShader(shader);
  }

  public static void glDeleteVertexArrays(int array) {
    if (!VulkanActive.active) GL33C.glDeleteVertexArrays(array);
  }

  public static void glEnableVertexAttribArray(int index) {
    if (!VulkanActive.active) GL33C.glEnableVertexAttribArray(index);
  }

  public static void glLinkProgram(int program) {
    if (!VulkanActive.active) GL33C.glLinkProgram(program);
  }

  public static void glPixelStorei(int pname, int param) {
    if (!VulkanActive.active) GL33C.glPixelStorei(pname, param);
  }

  public static void glShaderSource(int shader, CharSequence string) {
    if (!VulkanActive.active) GL33C.glShaderSource(shader, string);
  }

  public static void glTexParameteri(int target, int pname, int param) {
    if (!VulkanActive.active) GL33C.glTexParameteri(target, pname, param);
  }

  public static void glUniform1i(int location, int v0) {
    if (!VulkanActive.active) GL33C.glUniform1i(location, v0);
  }

  public static void glUniformMatrix4fv(int location, boolean transpose, float[] value) {
    if (!VulkanActive.active) GL33C.glUniformMatrix4fv(location, transpose, value);
  }

  public static void glUseProgram(int program) {
    if (!VulkanActive.active) GL33C.glUseProgram(program);
  }
}
