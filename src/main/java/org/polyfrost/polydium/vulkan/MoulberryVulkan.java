package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkQueryPoolCreateInfo;
import org.polyfrost.polydium.compat.Mods;
import org.polyfrost.polydium.vulkan.compat.GpuFormat;

public final class MoulberryVulkan {
  private static final List<GpuTextureView> VIEWS = new ArrayList<>();
  private static final Object2IntOpenHashMap<GpuTextureView> IDS = new Object2IntOpenHashMap<>();
  private static final IntArrayList GAPS = new IntArrayList();
  private static final IntOpenHashSet NEAREST = new IntOpenHashSet();
  private static final Map<GpuTexture, GpuTextureView> TEXTURE_VIEWS = new IdentityHashMap<>();
  private static final Map<String, Mod> MODS = new HashMap<>();
  private static GpuBuffer identity;

  static {
    IDS.defaultReturnValue(-1);
  }

  private MoulberryVulkan() {
  }

  private static final class Pipelines {
    private static final Identifier SHADER = Identifier.fromNamespaceAndPath("polydium", "core/imgui");
    private static final VertexFormat FORMAT = VertexFormat.builder()
      .add("Position", VulkanConst.vertexElement(VertexFormatElement.Type.FLOAT, 2))
      .add("UV0", VertexFormatElement.UV0)
      .add("Color", VertexFormatElement.COLOR)
      .build();
    private static final RenderPipeline IMGUI = pipeline("imgui", BlendFunction.TRANSLUCENT);
    private static final RenderPipeline COMPOSITE = pipeline("imgui_composite", new BlendFunction(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA));

    private static RenderPipeline pipeline(String name, BlendFunction blend) {
      RenderPipeline.Builder builder = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/" + name))
        .withVertexShader(SHADER)
        .withFragmentShader(SHADER)
        .withUniform("UBO", UniformType.UNIFORM_BUFFER)
        .withSampler("Sampler0")
        .withCull(false)
        .withVertexFormat(FORMAT, VertexFormat.Mode.TRIANGLES);
      VulkanRenderPipeline.withDepth(builder, null);
      return VulkanRenderPipeline.withColor(builder, new ColorTargetState(blend)).build();
    }
  }

  private static Mod mod(String name) {
    return MODS.computeIfAbsent(name, Mod::new);
  }

  public static void init(String mod) throws Throwable {
    Mod m = mod(mod);
    m.addBackendFlags.invoke(m.getIO.invoke(), 8);
    GpuDevice device = RenderSystem.getDevice();
    if (m.uniforms == null) {
      m.uniforms = device.createBuffer(() -> mod + " ImGui Uniforms", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 64L);
    }
  }

  public static void newFrame(String mod) throws Throwable {
    if (mod(mod).fontTexture == null) updateFontsTexture(mod);
  }

  public static void updateFontsTexture(String mod) throws Throwable {
    Mod m = mod(mod);
    destroyFontsTexture(mod);
    Object atlas = m.getFonts.invoke(m.getIO.invoke());
    Object width = m.newImInt.invoke();
    Object height = m.newImInt.invoke();
    ByteBuffer pixels = (ByteBuffer) m.getTexData.invoke(atlas, width, height);
    int w = (int) m.imIntGet.invoke(width);
    int h = (int) m.imIntGet.invoke(height);
    GpuDevice device = RenderSystem.getDevice();
    m.fontTexture = device.createTexture(mod + " ImGui Font Atlas", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.RGBA8, w, h, 1, 1);
    device.createCommandEncoder().writeToTexture(m.fontTexture, pixels, NativeImage.Format.RGBA, 0, 0, 0, 0, w, h);
    m.fontView = device.createTextureView(m.fontTexture);
    m.setTexId.invoke(atlas, (long) viewId(m.fontView));
  }

  public static void destroyFontsTexture(String mod) {
    Mod m = mod(mod);
    if (m.fontView != null) m.fontView.close();
    if (m.fontTexture != null) m.fontTexture.close();
    m.fontView = null;
    m.fontTexture = null;
  }

  public static Object renderDrawData(String mod, Object drawData) throws Throwable {
    Mod m = mod(mod);
    m.bind(drawData.getClass());
    int fbWidth = (int) ((float) m.displaySizeX.invoke(drawData) * (float) m.framebufferScaleX.invoke(drawData));
    int fbHeight = (int) ((float) m.displaySizeY.invoke(drawData) * (float) m.framebufferScaleY.invoke(drawData));
    int cmdListCount = (int) m.cmdListsCount.invoke(drawData);
    if (fbWidth <= 0 || fbHeight <= 0 || cmdListCount <= 0) return null;

    GpuDevice device = RenderSystem.getDevice();
    CommandEncoder encoder = device.createCommandEncoder();
    int drawIdxSize = (int) m.sizeOfImDrawIdx.invoke();
    long vertexBytes = (long) (int) m.totalVtxCount.invoke(drawData) * (int) m.sizeOfImDrawVert.invoke();
    long indexBytes = (long) (int) m.totalIdxCount.invoke(drawData) * drawIdxSize;
    if (vertexBytes >= Integer.MAX_VALUE || indexBytes >= Integer.MAX_VALUE) {
      throw new IllegalStateException("ImGui draw data too large: " + vertexBytes + " vertex bytes, " + indexBytes + " index bytes");
    }

    if (m.vertexHost == null || m.vertexHost.capacity() < vertexBytes) {
      m.vertexHost = ByteBuffer.allocateDirect((int) vertexBytes + 4096).order(ByteOrder.nativeOrder());
    }
    m.vertexHost.clear();
    for (int i = 0; i < cmdListCount; i++) m.vertexHost.put((ByteBuffer) m.vtxBufferData.invoke(drawData, i));
    m.vertexHost.flip();
    if (m.vertexBuffer != null && m.vertexBuffer.size() < vertexBytes) {
      m.vertexBuffer.close();
      m.vertexBuffer = null;
    }
    if (m.vertexBuffer == null) {
      m.vertexBuffer = device.createBuffer(() -> mod + " ImGui Vtx", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, vertexBytes + 4096L);
    }
    encoder.writeToBuffer(m.vertexBuffer.slice(0L, vertexBytes), m.vertexHost);

    if (m.indexHost == null || m.indexHost.capacity() < indexBytes) {
      m.indexHost = ByteBuffer.allocateDirect((int) indexBytes + 8192).order(ByteOrder.nativeOrder());
    }
    m.indexHost.clear();
    for (int i = 0; i < cmdListCount; i++) m.indexHost.put((ByteBuffer) m.idxBufferData.invoke(drawData, i));
    m.indexHost.flip();
    if (m.indexBuffer != null && m.indexBuffer.size() < indexBytes) {
      m.indexBuffer.close();
      m.indexBuffer = null;
    }
    if (m.indexBuffer == null) {
      m.indexBuffer = device.createBuffer(() -> mod + " ImGui Idx", GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, indexBytes + 8192L);
    }
    encoder.writeToBuffer(m.indexBuffer.slice(0L, indexBytes), m.indexHost);

    float displayX = (float) m.displayPosX.invoke(drawData);
    float displayY = (float) m.displayPosY.invoke(drawData);
    m.setupUniforms(encoder, displayX, displayY, (float) m.displaySizeX.invoke(drawData), (float) m.displaySizeY.invoke(drawData));
    m.target = resize(m.target, fbWidth, fbHeight, mod + " ImGui");

    float scaleX = (float) m.framebufferScaleX.invoke(drawData);
    float scaleY = (float) m.framebufferScaleY.invoke(drawData);
    GpuSampler linear = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
    GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
    long last = -1L;
    try (RenderPass pass = encoder.createRenderPass(() -> mod + " ImGui Render Pass", m.target.getColorTextureView(), OptionalInt.of(0))) {
      pass.setPipeline(Pipelines.IMGUI);
      pass.setVertexBuffer(0, m.vertexBuffer);
      pass.setIndexBuffer(m.indexBuffer, drawIdxSize == 2 ? VertexFormat.IndexType.SHORT : VertexFormat.IndexType.INT);
      pass.setUniform("UBO", m.uniforms);
      int globalVertexOffset = 0;
      int globalIndexOffset = 0;
      for (int cmdList = 0; cmdList < cmdListCount; cmdList++) {
        int cmdBufferSize = (int) m.cmdBufferSize.invoke(drawData, cmdList);
        for (int cmdBuf = 0; cmdBuf < cmdBufferSize; cmdBuf++) {
          long textureId = (long) m.cmdTextureId.invoke(drawData, cmdList, cmdBuf);
          if (textureId > 0L && textureId != last) {
            last = textureId;
            GpuTextureView view = textureId <= VIEWS.size() ? VIEWS.get((int) (textureId - 1L)) : null;
            if (view != null && !view.isClosed()) pass.bindTexture("Sampler0", view, NEAREST.contains((int) textureId) ? nearest : linear);
          }

          m.cmdClipRect.invoke(drawData, m.clipRect, cmdList, cmdBuf);
          int clipMinX = Math.max((int) (((float) m.vec4X.invoke(m.clipRect) - displayX) * scaleX), 0);
          int clipMinY = Math.max((int) (((float) m.vec4Y.invoke(m.clipRect) - displayY) * scaleY), 0);
          int clipMaxX = Math.min((int) (((float) m.vec4Z.invoke(m.clipRect) - displayX) * scaleX), fbWidth);
          int clipMaxY = Math.min((int) (((float) m.vec4W.invoke(m.clipRect) - displayY) * scaleY), fbHeight);
          if (clipMaxX < clipMinX || clipMaxY < clipMinY) continue;
          if (clipMinX == 0 && clipMaxX == fbWidth && clipMinY == 0 && clipMaxY == fbHeight) {
            pass.disableScissor();
          } else {
            pass.enableScissor(clipMinX, fbHeight - clipMaxY, clipMaxX - clipMinX, clipMaxY - clipMinY);
          }

          int indexCount = (int) m.cmdElemCount.invoke(drawData, cmdList, cmdBuf);
          int firstIndex = (int) m.cmdIdxOffset.invoke(drawData, cmdList, cmdBuf) + globalIndexOffset;
          int firstVertex = (int) m.cmdVtxOffset.invoke(drawData, cmdList, cmdBuf) + globalVertexOffset;
          pass.drawIndexed(firstVertex, firstIndex, indexCount, 1);
        }
        globalVertexOffset += (int) m.vtxBufferSize.invoke(drawData, cmdList);
        globalIndexOffset += (int) m.idxBufferSize.invoke(drawData, cmdList);
      }
    }

    cleanupUnusedTextures();
    m.drawn = m.target;
    return m.target;
  }

  public static int viewId(GpuTextureView view) {
    if (view.isClosed()) return 0;
    int existing = IDS.getInt(view);
    if (existing >= 0) return existing + 1;
    int next;
    if (!GAPS.isEmpty()) {
      next = GAPS.removeInt(GAPS.size() - 1);
      VIEWS.set(next, view);
    } else {
      next = VIEWS.size();
      VIEWS.add(view);
    }
    IDS.put(view, next);
    return next + 1;
  }

  public static int textureId(Object texture) {
    if (!VulkanActive.active) return ((GlTexture) texture).glId();
    GpuTexture gpuTexture = (GpuTexture) texture;
    if (gpuTexture.isClosed()) return 0;
    return viewId(TEXTURE_VIEWS.computeIfAbsent(gpuTexture, RenderSystem.getDevice()::createTextureView));
  }

  public static long getTextureId(Object view) {
    return viewId((GpuTextureView) view);
  }

  public static void setSampleNearest(long id) {
    NEAREST.add((int) id);
  }

  public static void setSampleLinear(long id) {
    NEAREST.remove((int) id);
  }

  private static void cleanupUnusedTextures() {
    TEXTURE_VIEWS.values().removeIf(view -> {
      if (!view.texture().isClosed()) return false;
      view.close();
      return true;
    });
    for (int i = 0; i < VIEWS.size(); i++) {
      GpuTextureView view = VIEWS.get(i);
      if (view != null && view.isClosed()) {
        GAPS.add(i);
        VIEWS.set(i, null);
        IDS.removeInt(view);
        NEAREST.remove(i + 1);
      }
    }
  }

  public static RenderTarget composite(RenderTarget main) throws Throwable {
    RenderTarget out = main;
    for (String name : new String[]{"flashback", "axiom"}) {
      if (Mods.loaded(name) && !mod(name).selfComposite) out = mod(name).composite(out);
    }
    return out == main ? null : out;
  }

  private static RenderTarget resize(RenderTarget target, int width, int height, String label) {
    if (target == null) return new TextureTarget(label, width, height, false);
    if (target.width != width || target.height != height) target.resize(width, height);
    return target;
  }

  public static int glGetInteger(int pname) {
    return VulkanActive.active ? 0 : GL11.glGetInteger(pname);
  }

  public static void _glBindFramebuffer(int target, int framebuffer) {
    if (!VulkanActive.active) GlStateManager._glBindFramebuffer(target, framebuffer);
  }

  public static void _viewport(int x, int y, int width, int height) {
    if (!VulkanActive.active) GlStateManager._viewport(x, y, width, height);
  }

  public static void _bindTexture(int texture) {
    if (VulkanActive.active) boundTexture = texture;
    else GlStateManager._bindTexture(texture);
  }

  public static void glGetIntegerv(int pname, int[] params) {
    if (!VulkanActive.active) GL11.glGetIntegerv(pname, params);
  }

  public static void glTexParameteri(int target, int pname, int param) {
    if (!VulkanActive.active) GL11.glTexParameteri(target, pname, param);
    else if (pname == GL11.GL_TEXTURE_MIN_FILTER || pname == GL11.GL_TEXTURE_MAG_FILTER) {
      if (param == GL11.GL_NEAREST) setSampleNearest(boundTexture);
      else setSampleLinear(boundTexture);
    }
  }

  public static void _deleteTexture(int texture) {
    if (!VulkanActive.active) GlStateManager._deleteTexture(texture);
  }

  private static int cullFace = GL11.GL_BACK;
  private static int boundTexture;

  public static void glCullFace(int mode) {
    if (VulkanActive.active) cullFace = mode;
    else GL11.glCullFace(mode);
  }

  public static void tagCullFace(RenderPipeline pipeline) {
    if (VulkanActive.active && cullFace == GL11.GL_FRONT) VulkanActive.CULL_FRONT.add(pipeline);
  }

  private static Class<?> axiomPipelineExt;
  private static MethodHandle axiomCullFront;

  public static boolean cullFront(RenderPipeline pipeline) {
    if (VulkanActive.CULL_FRONT.contains(pipeline)) return true;
    if (!Mods.loaded("axiom")) return false;
    try {
      if (axiomPipelineExt == null) try {
        axiomPipelineExt = Class.forName("com.moulberry.axiom.hooks.RenderPipelineExt");
        axiomCullFront = MethodHandles.publicLookup().findVirtual(axiomPipelineExt, "axiom$isCullFrontFaces", MethodType.methodType(boolean.class));
      } catch (ClassNotFoundException | NoSuchMethodException e) {
        axiomPipelineExt = Void.class;
      }
      return axiomCullFront != null && axiomPipelineExt.isInstance(pipeline) && (boolean) axiomCullFront.invoke(pipeline);
    } catch (Throwable t) {
      throw new IllegalStateException("Axiom RenderPipelineExt", t);
    }
  }

  public static GpuFormat colorFormat(RenderPipeline pipeline) {
    return pipeline.getLocation().toString().equals("flashback:pipeline/flashback_blit_depth") ? GpuFormat.R32_FLOAT : GpuFormat.RGBA8_UNORM;
  }

  private static RenderTarget boundDepth;
  private static GpuBuffer depthBuffer;

  public static void bindDepth(Object target) {
    boundDepth = (RenderTarget) target;
  }

  public static void glReadPixels(int x, int y, int width, int height, int format, int type, float[] pixels) {
    if (!VulkanActive.active) {
      GL11.glReadPixels(x, y, width, height, format, type, pixels);
      return;
    }
    GpuDevice device = RenderSystem.getDevice();
    long bytes = 4L * width * height;
    if (depthBuffer == null || depthBuffer.size() < bytes) {
      if (depthBuffer != null) depthBuffer.close();
      depthBuffer = device.createBuffer(() -> "Axiom depth readback", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, bytes);
    }
    CommandEncoder encoder = device.createCommandEncoder();
    encoder.copyTextureToBuffer(boundDepth.getDepthTexture(), depthBuffer, 0L, () -> {}, 0, x, y, width, height);
    XaeroVulkan.finish();
    try (GpuBuffer.MappedView view = encoder.mapBuffer(depthBuffer.slice(0L, bytes), true, false)) {
      view.data().asFloatBuffer().get(pixels, 0, width * height);
    }
    if (VulkanActive.zZeroToOne()) for (int i = 0; i < width * height; i++) pixels[i] = (pixels[i] + 1.0F) * 0.5F;
  }

  public static CompletableFuture<float[]> readDepth(Object target, int x, int y, int width, int height) {
    CompletableFuture<float[]> future = new CompletableFuture<>();
    GpuDevice device = RenderSystem.getDevice();
    long bytes = 4L * width * height;
    GpuBuffer buffer = device.createBuffer(() -> "Axiom depth readback", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, bytes);
    CommandEncoder encoder = device.createCommandEncoder();
    encoder.copyTextureToBuffer(((RenderTarget) target).getDepthTexture(), buffer, 0L, () -> {
      try (GpuBuffer.MappedView view = encoder.mapBuffer(buffer.slice(0L, bytes), true, false)) {
        float[] pixels = new float[width * height];
        view.data().asFloatBuffer().get(pixels);
        future.complete(pixels);
      } finally {
        buffer.close();
      }
    }, 0, x, y, width, height);
    return future;
  }

  private static final Map<Object, GpuBuffer> FLASHBACK_DOWNLOADS = new WeakHashMap<>();

  public static void startDownload(Object owner, GpuTexture texture, int width, int height) {
    long bytes = 4L * width * height;
    GpuBuffer buffer = FLASHBACK_DOWNLOADS.get(owner);
    if (buffer == null || buffer.size() < bytes) {
      if (buffer != null) buffer.close();
      buffer = RenderSystem.getDevice().createBuffer(() -> "Flashback texture output buffer", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, bytes);
      FLASHBACK_DOWNLOADS.put(owner, buffer);
    }
    RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture, buffer, 0L, () -> {}, 0);
  }

  public static Object finishDownload(Object owner, int width, int height) {
    XaeroVulkan.finish();
    NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
    try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(FLASHBACK_DOWNLOADS.get(owner).slice(0L, 4L * width * height), true, false)) {
      MemoryUtil.memCopy(MemoryUtil.memAddress(view.data()), image.getPointer(), 4L * width * height);
    }
    return image;
  }

  public static void closeDownload(Object owner) {
    GpuBuffer buffer = FLASHBACK_DOWNLOADS.remove(owner);
    if (buffer != null) buffer.close();
  }

  private static final Map<Integer, Long> QUERY_POOLS = new HashMap<>();
  private static int nextQuery = 1;
  private static long activePool;

  public static int glGenQueries() {
    if (!VulkanActive.active) return GL31C.glGenQueries();
    VulkanDevice device = VulkanDevice.active;
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkQueryPoolCreateInfo createInfo = VkQueryPoolCreateInfo.calloc(stack).sType$Default().queryType(VK12.VK_QUERY_TYPE_OCCLUSION).queryCount(1);
      LongBuffer pointer = stack.callocLong(1);
      VulkanUtils.crashIfFailure(device, VK12.vkCreateQueryPool(device.vkDevice(), createInfo, null, pointer), "Cannot create occlusion query pool");
      QUERY_POOLS.put(nextQuery, pointer.get(0));
    }
    return nextQuery++;
  }

  public static void glBeginQuery(int target, int id) {
    if (!VulkanActive.active) {
      GL31C.glBeginQuery(target, id);
      return;
    }
    activePool = QUERY_POOLS.get(id);
    VkCommandBuffer commandBuffer = VulkanDevice.active.createCommandEncoder().commandBuffer();
    VK12.vkCmdResetQueryPool(commandBuffer, activePool, 0, 1);
    VK12.vkCmdBeginQuery(commandBuffer, activePool, 0, 0);
  }

  public static void glEndQuery(int target) {
    if (!VulkanActive.active) GL31C.glEndQuery(target);
    else VK12.vkCmdEndQuery(VulkanDevice.active.createCommandEncoder().commandBuffer(), activePool, 0);
  }

  public static int glGetQueryObjecti(int id, int pname) {
    if (!VulkanActive.active) return GL31C.glGetQueryObjecti(id, pname);
    XaeroVulkan.finish();
    try (MemoryStack stack = MemoryStack.stackPush()) {
      LongBuffer result = stack.callocLong(1);
      VulkanUtils.crashIfFailure(VulkanDevice.active,
        VK12.vkGetQueryPoolResults(VulkanDevice.active.vkDevice(), QUERY_POOLS.get(id), 0, 1, result, 8L, VK12.VK_QUERY_RESULT_64_BIT | VK12.VK_QUERY_RESULT_WAIT_BIT),
        "Cannot fetch query result");
      return (int) Math.min(result.get(0), Integer.MAX_VALUE);
    }
  }

  public static void glDeleteQueries(int id) {
    if (!VulkanActive.active) {
      GL31C.glDeleteQueries(id);
      return;
    }
    Long pool = QUERY_POOLS.remove(id);
    if (pool != null) VK12.vkDestroyQueryPool(VulkanDevice.active.vkDevice(), pool, null);
  }

  private static MethodHandle axiomChunksChanged;

  public static boolean axiomChunksChanged() {
    if (!Mods.loaded("axiom")) return false;
    try {
      if (axiomChunksChanged == null) {
        axiomChunksChanged = MethodHandles.publicLookup().findStatic(Class.forName("com.moulberry.axiom.render.ChunkRenderOverrider"),
          "hasOverridenChunksChanged", MethodType.methodType(boolean.class));
      }
      return (boolean) axiomChunksChanged.invokeExact();
    } catch (Throwable t) {
      throw new IllegalStateException("Axiom ChunkRenderOverrider", t);
    }
  }

  private static MethodHandle exportJob;
  private static MethodHandle exportSettings;
  private static MethodHandle exportProjection;
  private static boolean noExportProjection;

  public static boolean useBlockFaceCulling(boolean enabled) {
    if (!enabled || noExportProjection || !Mods.loaded("flashback")) return enabled;
    try {
      if (exportJob == null) try {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        Class<?> job = Class.forName("com.moulberry.flashback.exporting.ExportJob");
        Class<?> settings = Class.forName("com.moulberry.flashback.exporting.ExportSettings");
        exportSettings = lookup.findVirtual(job, "getSettings", MethodType.methodType(settings));
        exportProjection = lookup.findVirtual(settings, "projection", MethodType.methodType(Class.forName("com.moulberry.flashback.combo_options.ExportProjection")));
        exportJob = lookup.findStaticGetter(Class.forName("com.moulberry.flashback.Flashback"), "EXPORT_JOB", job);
      } catch (ClassNotFoundException | NoSuchMethodException e) {
        noExportProjection = true;
        return enabled;
      }
      Object job = exportJob.invoke();
      return job == null || !"ORTHOGRAPHIC".equals(((Enum<?>) exportProjection.invoke(exportSettings.invoke(job))).name());
    } catch (Throwable t) {
      throw new IllegalStateException("Flashback export settings", t);
    }
  }

  private static final class Mod {
    final String name;
    final MethodHandle getIO, drawOverlay, isActive, inGameUi, windowWidth, windowHeight;
    final MethodHandle frameX, frameY, frameWidth, frameHeight, viewportSizeX, viewportSizeY;
    final MethodHandle addBackendFlags, getFonts, getTexData, setTexId, newImInt, imIntGet;
    MethodHandle displaySizeX, displaySizeY, displayPosX, displayPosY, framebufferScaleX, framebufferScaleY;
    MethodHandle cmdListsCount, totalVtxCount, totalIdxCount, sizeOfImDrawVert, sizeOfImDrawIdx, vtxBufferData, idxBufferData;
    MethodHandle cmdBufferSize, cmdTextureId, cmdClipRect, cmdElemCount, cmdIdxOffset, cmdVtxOffset, vtxBufferSize, idxBufferSize;
    MethodHandle vec4X, vec4Y, vec4Z, vec4W;
    Class<?> drawDataClass;
    Object clipRect;
    GpuBuffer uniforms, vertexBuffer, indexBuffer, quads;
    ByteBuffer vertexHost, indexHost;
    final ByteBuffer uniformHost = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());
    final ByteBuffer quadHost = ByteBuffer.allocateDirect(2 * 6 * 20).order(ByteOrder.nativeOrder());
    final float[] matrix = new float[16];
    final float[] uploaded = new float[16];
    GpuTexture fontTexture;
    GpuTextureView fontView;
    RenderTarget target, composite, drawn;
    final boolean selfComposite;

    Mod(String name) {
      this.name = name;
      boolean flashback = name.equals("flashback");
      try {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        Class<?> ui = Class.forName(flashback ? "com.moulberry.flashback.editor.ui.ReplayUI" : "com.moulberry.axiom.editor.EditorUI");
        Class<?> tracker = Class.forName(flashback ? "com.moulberry.flashback.WindowSizeTracker" : "com.moulberry.axiom.utils.WindowSizeTracker");
        Method io = ui.getMethod("getIO");
        String pkg = io.getReturnType().getPackageName();
        Class<?> imInt = Class.forName(pkg + ".type.ImInt", true, ui.getClassLoader());
        Class<?> atlas = Class.forName(pkg + ".ImFontAtlas", true, ui.getClassLoader());
        this.getIO = lookup.unreflect(io);
        this.drawOverlay = lookup.findStatic(ui, "drawOverlay", MethodType.methodType(void.class));
        this.isActive = lookup.findStatic(ui, "isActive", MethodType.methodType(boolean.class));
        this.inGameUi = flashback ? null
          : lookup.findStatic(Class.forName("com.moulberry.axiom.editor.ImageReferenceWindows"), "hasReferenceInGameUI", MethodType.methodType(boolean.class));
        this.windowWidth = lookup.findStatic(tracker, "getWidth", MethodType.methodType(int.class, Window.class));
        this.windowHeight = lookup.findStatic(tracker, "getHeight", MethodType.methodType(int.class, Window.class));
        this.frameX = lookup.findStaticGetter(ui, "frameX", int.class);
        this.frameY = lookup.findStaticGetter(ui, "frameY", int.class);
        this.frameWidth = lookup.findStaticGetter(ui, "frameWidth", int.class);
        this.frameHeight = lookup.findStaticGetter(ui, "frameHeight", int.class);
        this.viewportSizeX = lookup.findStaticGetter(ui, "viewportSizeX", int.class);
        this.viewportSizeY = lookup.findStaticGetter(ui, "viewportSizeY", int.class);
        this.addBackendFlags = lookup.findVirtual(io.getReturnType(), "addBackendFlags", MethodType.methodType(void.class, int.class));
        this.getFonts = lookup.findVirtual(io.getReturnType(), "getFonts", MethodType.methodType(atlas));
        this.getTexData = lookup.findVirtual(atlas, "getTexDataAsRGBA32", MethodType.methodType(ByteBuffer.class, imInt, imInt));
        this.setTexId = virtualOr(lookup, atlas, "setTexID", MethodType.methodType(void.class, long.class), MethodType.methodType(void.class, int.class));
        this.newImInt = lookup.findConstructor(imInt, MethodType.methodType(void.class));
        this.imIntGet = lookup.findVirtual(imInt, "get", MethodType.methodType(int.class));
        this.selfComposite = Arrays.stream(ui.getFields()).anyMatch(field -> field.getName().equals("compositeOnTop"));
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Unsupported " + name + " version for Vulkan", e);
      }
    }

    void bind(Class<?> drawData) throws ReflectiveOperationException {
      if (drawData == this.drawDataClass) return;
      MethodHandles.Lookup lookup = MethodHandles.publicLookup();
      MethodType f = MethodType.methodType(float.class);
      MethodType i = MethodType.methodType(int.class);
      MethodType iI = MethodType.methodType(int.class, int.class);
      MethodType iII = MethodType.methodType(int.class, int.class, int.class);
      MethodType bI = MethodType.methodType(ByteBuffer.class, int.class);
      Class<?> vec4 = Class.forName(drawData.getPackageName() + ".ImVec4", true, drawData.getClassLoader());
      this.displaySizeX = lookup.findVirtual(drawData, "getDisplaySizeX", f);
      this.displaySizeY = lookup.findVirtual(drawData, "getDisplaySizeY", f);
      this.displayPosX = lookup.findVirtual(drawData, "getDisplayPosX", f);
      this.displayPosY = lookup.findVirtual(drawData, "getDisplayPosY", f);
      this.framebufferScaleX = lookup.findVirtual(drawData, "getFramebufferScaleX", f);
      this.framebufferScaleY = lookup.findVirtual(drawData, "getFramebufferScaleY", f);
      this.cmdListsCount = lookup.findVirtual(drawData, "getCmdListsCount", i);
      this.totalVtxCount = lookup.findVirtual(drawData, "getTotalVtxCount", i);
      this.totalIdxCount = lookup.findVirtual(drawData, "getTotalIdxCount", i);
      this.sizeOfImDrawVert = lookup.findStatic(drawData, "sizeOfImDrawVert", i);
      this.sizeOfImDrawIdx = lookup.findStatic(drawData, "sizeOfImDrawIdx", i);
      this.vtxBufferData = lookup.findVirtual(drawData, "getCmdListVtxBufferData", bI);
      this.idxBufferData = lookup.findVirtual(drawData, "getCmdListIdxBufferData", bI);
      this.vtxBufferSize = lookup.findVirtual(drawData, "getCmdListVtxBufferSize", iI);
      this.idxBufferSize = lookup.findVirtual(drawData, "getCmdListIdxBufferSize", iI);
      this.cmdBufferSize = lookup.findVirtual(drawData, "getCmdListCmdBufferSize", iI);
      this.cmdTextureId = virtualOr(lookup, drawData, "getCmdListCmdBufferTextureId", iII.changeReturnType(long.class), iII);
      MethodType clip = MethodType.methodType(void.class, vec4, int.class, int.class);
      try {
        this.cmdClipRect = lookup.findVirtual(drawData, "getCmdListCmdBufferClipRect", clip);
      } catch (NoSuchMethodException e) {
        this.cmdClipRect = MethodHandles.permuteArguments(lookup.findVirtual(drawData, "getCmdListCmdBufferClipRect",
          MethodType.methodType(void.class, int.class, int.class, vec4)), clip.insertParameterTypes(0, drawData), 0, 2, 3, 1);
      }
      this.cmdElemCount = lookup.findVirtual(drawData, "getCmdListCmdBufferElemCount", iII);
      this.cmdIdxOffset = lookup.findVirtual(drawData, "getCmdListCmdBufferIdxOffset", iII);
      this.cmdVtxOffset = lookup.findVirtual(drawData, "getCmdListCmdBufferVtxOffset", iII);
      this.vec4X = lookup.findGetter(vec4, "x", float.class);
      this.vec4Y = lookup.findGetter(vec4, "y", float.class);
      this.vec4Z = lookup.findGetter(vec4, "z", float.class);
      this.vec4W = lookup.findGetter(vec4, "w", float.class);
      try {
        this.clipRect = lookup.findConstructor(vec4, MethodType.methodType(void.class)).invoke();
      } catch (Throwable t) {
        throw new IllegalStateException(t);
      }
      this.drawDataClass = drawData;
    }

    private static MethodHandle virtualOr(MethodHandles.Lookup lookup, Class<?> owner, String name, MethodType want, MethodType old) throws ReflectiveOperationException {
      try {
        return lookup.findVirtual(owner, name, want);
      } catch (NoSuchMethodException e) {
        return MethodHandles.explicitCastArguments(lookup.findVirtual(owner, name, old), want.insertParameterTypes(0, owner));
      }
    }

    void setupUniforms(CommandEncoder encoder, float x, float y, float width, float height) {
      float l = x, r = x + width, t = y, b = y + height;
      if (VulkanActive.zZeroToOne()) {
        this.matrix[10] = 0.5F;
        this.matrix[14] = 0.5F;
      } else {
        this.matrix[10] = 1.0F;
        this.matrix[14] = 0.0F;
      }
      this.matrix[15] = 1.0F;
      this.matrix[0] = 2.0F / (r - l);
      this.matrix[5] = 2.0F / (t - b);
      this.matrix[12] = (r + l) / (l - r);
      this.matrix[13] = (t + b) / (b - t);
      if (Arrays.equals(this.matrix, this.uploaded)) return;
      System.arraycopy(this.matrix, 0, this.uploaded, 0, 16);
      this.uniformHost.clear();
      this.uniformHost.asFloatBuffer().put(this.matrix);
      encoder.writeToBuffer(this.uniforms.slice(), this.uniformHost);
    }

    RenderTarget composite(RenderTarget main) throws Throwable {
      this.drawn = null;
      if (RenderSystem.isOnRenderThread()) this.drawOverlay.invoke();
      boolean editor = (boolean) this.isActive.invoke();
      if (!(editor || this.inGameUi != null && (boolean) this.inGameUi.invoke()) || this.drawn == null) return main;

      Window window = Minecraft.getInstance().getWindow();
      this.composite = resize(this.composite, (int) this.windowWidth.invoke(window), (int) this.windowHeight.invoke(window), this.name + " composite");
      boolean blitFrame = true;
      float x1 = 0.0F, y1 = 0.0F, x2 = 1.0F, y2 = 1.0F;
      if (editor) {
        int frameWidth = (int) this.frameWidth.invoke();
        int frameHeight = (int) this.frameHeight.invoke();
        blitFrame = frameWidth > 1 && frameHeight > 1;
        float viewportX = (int) this.viewportSizeX.invoke();
        float viewportY = (int) this.viewportSizeY.invoke();
        x1 = (int) this.frameX.invoke() / viewportX;
        y1 = (int) this.frameY.invoke() / viewportY;
        x2 = x1 + frameWidth / viewportX;
        y2 = y1 + frameHeight / viewportY;
      }

      ByteBuffer vertices = this.quadHost.clear();
      quad(vertices, x1, y1, x2, y2);
      quad(vertices, 0.0F, 0.0F, 1.0F, 1.0F);
      vertices.flip();
      GpuDevice device = RenderSystem.getDevice();
      CommandEncoder encoder = device.createCommandEncoder();
      if (this.quads == null) {
        this.quads = device.createBuffer(() -> this.name + " composite quads", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, vertices.remaining());
      }
      encoder.writeToBuffer(this.quads.slice(), vertices);
      if (identity == null) {
        ByteBuffer matrix = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());
        matrix.asFloatBuffer().put(new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});
        identity = device.createBuffer(() -> "ImGui composite identity", GpuBuffer.USAGE_UNIFORM, matrix);
      }

      try (RenderPass pass = encoder.createRenderPass(() -> this.name + " composite", this.composite.getColorTextureView(), OptionalInt.of(0))) {
        pass.setPipeline(Pipelines.COMPOSITE);
        pass.setUniform("UBO", identity);
        pass.setVertexBuffer(0, this.quads);
        if (blitFrame) {
          pass.bindTexture("Sampler0", main.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
          pass.draw(0, 6);
        }
        pass.bindTexture("Sampler0", this.drawn.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
        pass.draw(6, 6);
      }
      return this.composite;
    }

    private static void quad(ByteBuffer buffer, float x1, float y1, float x2, float y2) {
      float left = x1 * 2.0F - 1.0F, right = x2 * 2.0F - 1.0F, bottom = -(y2 * 2.0F - 1.0F), top = -(y1 * 2.0F - 1.0F);
      float[][] corners = {{left, bottom, 0, 0}, {right, bottom, 1, 0}, {right, top, 1, 1}, {left, bottom, 0, 0}, {right, top, 1, 1}, {left, top, 0, 1}};
      for (float[] corner : corners) {
        buffer.putFloat(corner[0]).putFloat(corner[1]).putFloat(corner[2]).putFloat(corner[3]).putInt(-1);
      }
    }
  }
}
