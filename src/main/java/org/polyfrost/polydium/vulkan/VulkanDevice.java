package org.polyfrost.polydium.vulkan;

import org.polyfrost.polydium.vulkan.compat.SurfaceException;
import org.polyfrost.polydium.vulkan.compat.GpuSurface;
import org.polyfrost.polydium.vulkan.compat.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import org.polyfrost.polydium.vulkan.compat.DeviceFeatures;
import org.polyfrost.polydium.vulkan.compat.DeviceInfo;
import org.polyfrost.polydium.vulkan.compat.DeviceLimits;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import org.polyfrost.polydium.vulkan.compat.GpuQueryPool;
import org.polyfrost.polydium.vulkan.compat.GpuSurfaceBackend;
import org.polyfrost.polydium.vulkan.compat.HintsAndWorkarounds;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import org.polyfrost.polydium.vulkan.checkpoints.CheckpointExtension;
import org.polyfrost.polydium.vulkan.glsl.GlslCompiler;
import org.polyfrost.polydium.vulkan.glsl.IntermediaryShaderModule;
import org.polyfrost.polydium.vulkan.glsl.ShaderCompileException;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
//? if < 26.1 {
/*import java.util.regex.Matcher;
import net.minecraft.util.Mth;
*///?}
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.vma.Vma;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceLimits;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Properties;
import org.slf4j.Logger;

public class VulkanDevice implements GpuDeviceBackend {
  public static VulkanDevice active;
  private static final Logger LOGGER = LogUtils.getLogger();
  private final ShaderSource defaultShaderSource;
  private final Map<RenderPipeline, VulkanRenderPipeline> pipelineCache = new IdentityHashMap<>();
  private final Map<VulkanDevice.ShaderCompilationKey, IntermediaryShaderModule> shaderCache = new HashMap<>();
  private final VulkanInstance instance;
  private final VkDevice vkDevice;
  private final long vma;
  private final GlslCompiler glslCompiler = new GlslCompiler();
  private final DeviceInfo deviceInfo;
  private final VulkanQueue graphicsQueue;
  private final VulkanQueue computeQueue;
  private final VulkanQueue transferQueue;
  private final boolean isIntegratedIntelMoltenVK;
  private final boolean fillModeNonSolid;
  private final VulkanCommandEncoder commandEncoder;
  private final CheckpointExtension checkpointExtension;

  public VulkanDevice(
    final ShaderSource defaultShaderSource,
    final VulkanInstance instance,
    final VulkanPhysicalDevice physicalDevice,
    final Set<String> enabledDeviceExtensions,
    final VkDevice vkDevice,
    final long vma,
    final CheckpointExtension checkpointExtension
  ) {
    this.defaultShaderSource = defaultShaderSource;
    this.instance = instance;
    this.vkDevice = vkDevice;
    this.vma = vma;
    this.checkpointExtension = checkpointExtension;
    this.fillModeNonSolid = physicalDevice.supportsFillModeNonSolid();
    Set<String> extensionNames = new HashSet<>();

    for (String name : instance.getEnabledExtensions()) {
      extensionNames.add(name + " (I)");
    }

    for (String name : enabledDeviceExtensions) {
      extensionNames.add(name + " (D)");
    }

    VkPhysicalDeviceLimits limits = physicalDevice.vkPhysicalDeviceProperties().limits();
    VkPhysicalDeviceVulkan11Properties vk11Properties = physicalDevice.vkPhysicalDeviceVulkan11Properties();
    this.deviceInfo = new DeviceInfo(
      physicalDevice.deviceName(),
      physicalDevice.vendorName(),
      physicalDevice.driverInfo(),
      true,
      "Vulkan",
      limits.timestampPeriod(),
      new DeviceLimits(
        (int)limits.maxSamplerAnisotropy(),
        (int)limits.minUniformBufferOffsetAlignment(),
        limits.maxImageDimension2D(),
        vk11Properties.maxMemoryAllocationSize() < 0L ? 9223372036854775807L : vk11Properties.maxMemoryAllocationSize(),
        physicalDevice.vkPhysicalDeviceMultiDrawPropertiesEXT().maxMultiDrawCount() < 0
          ? 2147483647
          : physicalDevice.vkPhysicalDeviceMultiDrawPropertiesEXT().maxMultiDrawCount(),
        limits.maxColorAttachments()
      ),
      new DeviceFeatures(true, enabledDeviceExtensions.contains("VK_EXT_multi_draw"), false, true, true, true, true),
      Collections.unmodifiableSet(extensionNames),
      new HintsAndWorkarounds(false, false),
      physicalDevice.deviceType()
    );
    IntIntPair graphicsQueueFamily = physicalDevice.graphicsQueueFamilyAndIndex();

    assert graphicsQueueFamily != null;

    IntIntPair computeQueueFamily = physicalDevice.computeQueueFamilyAndIndex();
    IntIntPair transferQueueFamily = physicalDevice.transferQueueFamilyAndIndex();
    this.graphicsQueue = new VulkanQueue(this, graphicsQueueFamily.leftInt(), graphicsQueueFamily.rightInt());
    if (computeQueueFamily != null) {
      this.computeQueue = new VulkanQueue(this, computeQueueFamily.leftInt(), computeQueueFamily.rightInt());
    } else {
      this.computeQueue = this.graphicsQueue;
    }

    if (transferQueueFamily != null) {
      this.transferQueue = new VulkanQueue(this, transferQueueFamily.leftInt(), transferQueueFamily.rightInt());
    } else {
      this.transferQueue = this.computeQueue;
    }

    this.isIntegratedIntelMoltenVK = physicalDevice.vkPhysicalDeviceProperties().deviceType() == 1
      && physicalDevice.vkPhysicalDeviceProperties().vendorID() == 32902
      && physicalDevice.vkPhysicalDeviceDriverProperties().driverID() == 14;
    physicalDevice.close();
    this.commandEncoder = new VulkanCommandEncoder(this);
    active = this;
    VulkanActive.active = true;
  }

  @Override
  public void close() {
    this.graphicsQueue.waitIdle();
    if (this.surface != null) {
      this.surface.close();
    }

    this.checkpointExtension.close();
    this.commandEncoder.destroy();
    this.clearPipelineCache();
    Vma.vmaDestroyAllocator(this.vma);
    VK12.vkDestroyDevice(this.vkDevice, null);
    this.instance.close();
    this.glslCompiler.close();
  }

  public DeviceInfo getDeviceInfo() {
    return this.deviceInfo;
  }

  public VulkanInstance instance() {
    return this.instance;
  }

  public boolean fillModeNonSolid() {
    return this.fillModeNonSolid;
  }

  public VkDevice vkDevice() {
    return this.vkDevice;
  }

  public VulkanQueue graphicsQueue() {
    return this.graphicsQueue;
  }

  public VulkanQueue computeQueue() {
    return this.computeQueue;
  }

  public VulkanQueue transferQueue() {
    return this.transferQueue;
  }

  public long vma() {
    return this.vma;
  }

  public GpuSurfaceBackend createSurface(final long windowHandle) {
    return new VulkanGpuSurface(this, windowHandle);
  }

  public VulkanCommandEncoder createCommandEncoder() {
    return this.commandEncoder;
  }

  @Override
  public GpuSampler createSampler(
    final AddressMode addressModeU,
    final AddressMode addressModeV,
    final FilterMode minFilter,
    final FilterMode magFilter,
    final int maxAnisotropy,
    final OptionalDouble maxLod
  ) {
    return new VulkanGpuSampler(this, addressModeU, addressModeV, minFilter, magFilter, maxAnisotropy, maxLod);
  }

  @Override
  public GpuTexture createTexture(
    final @Nullable Supplier<String> label,
    final @GpuTexture.Usage int usage,
    final TextureFormat format,
    final int width,
    final int height,
    final int depthOrLayers,
    final int mipLevels
  ) {
    return new VulkanGpuTexture(this, usage, this.isDebuggingEnabled() && label != null ? label.get() : "", VulkanConst.toGpuFormat(format), width, height, depthOrLayers, maxMipLevels(width, height, mipLevels));
  }

  @Override
  public GpuTexture createTexture(
    final @Nullable String label,
    final @GpuTexture.Usage int usage,
    final TextureFormat format,
    final int width,
    final int height,
    final int depthOrLayers,
    final int mipLevels
  ) {
    return new VulkanGpuTexture(this, usage, this.isDebuggingEnabled() && label != null ? label : "", VulkanConst.toGpuFormat(format), width, height, depthOrLayers, maxMipLevels(width, height, mipLevels));
  }

  private static int maxMipLevels(final int width, final int height, final int mipLevels) {
    //? if >= 26.1 {
    return mipLevels;
    //?} else {
    /*return Math.min(mipLevels, Mth.log2(Math.max(width, height)) + 1);
    *///?}
  }

  @Override
  public GpuTextureView createTextureView(final GpuTexture texture) {
    return this.createTextureView(texture, 0, texture.getMipLevels());
  }

  @Override
  public GpuTextureView createTextureView(final GpuTexture texture, final int baseMipLevel, final int mipLevels) {
    return new VulkanGpuTextureView(this, (VulkanGpuTexture)texture, baseMipLevel, mipLevels);
  }

  public VulkanGpuBuffer createBuffer(final @Nullable Supplier<String> label, final @GpuBuffer.Usage int usage, final long size) {
    return new VulkanGpuBuffer.Direct(this, label, usage, size, this.isIntegratedIntelMoltenVK);
  }

  @Override
  public GpuBuffer createBuffer(final @Nullable Supplier<String> label, final @GpuBuffer.Usage int usage, final ByteBuffer data) {
    VulkanGpuBuffer buffer = this.createBuffer(label, usage | 8, (long)data.remaining());
    VulkanCommandEncoder encoder = (VulkanCommandEncoder)this.createCommandEncoder();
    GpuBufferSlice stagingBuffer = encoder.transientMemory().uploadStaging(data, 1L, 16);
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkBufferCopy.Buffer regions = VkBufferCopy.calloc(1, stack).srcOffset(stagingBuffer.offset()).dstOffset(0L).size(data.remaining());
      VK12.vkCmdCopyBuffer(encoder.objectInitCommandBuffer(), ((VulkanGpuBuffer)stagingBuffer.buffer()).vkBuffer(), buffer.vkBuffer(), regions);
    }
    return buffer;
  }

  @Override
  public List<String> getLastDebugMessages() {
    return List.of();
  }

  @Override
  public boolean isDebuggingEnabled() {
    return this.instance.debug().enabled();
  }

  @Override
  public CompiledRenderPipeline precompilePipeline(final RenderPipeline pipeline, final @Nullable ShaderSource customShaderSource) {
    ShaderSource shaderSource = customShaderSource == null ? this.defaultShaderSource : customShaderSource;
    return this.pipelineCache.computeIfAbsent(pipeline, ignored -> this.compilePipeline(pipeline, shaderSource));
  }

  protected VulkanRenderPipeline getOrCompilePipeline(final RenderPipeline pipeline) {
    return this.pipelineCache.computeIfAbsent(pipeline, ignored -> this.compilePipeline(pipeline, this.defaultShaderSource));
  }

  protected IntermediaryShaderModule getOrCompileShader(
    final Identifier id, final ShaderType type, final ShaderDefines defines, final ShaderSource shaderSource
  ) {
    VulkanDevice.ShaderCompilationKey key = new VulkanDevice.ShaderCompilationKey(id, type, defines);
    return this.shaderCache.computeIfAbsent(key, ignored -> this.compileShader(key, shaderSource));
  }

  private static final Pattern RESERVED_SAMPLER = Pattern.compile("\\bsampler\\b");
  //? if < 26.1 {
  /*private static final Pattern VERTEX_MAIN = Pattern.compile("\\bvoid\\s+main\\s*\\(\\s*(void)?\\s*\\)");

  private static String glToVulkanDepth(final String source) {
    Matcher main = VERTEX_MAIN.matcher(source);
    if (!main.find()) {
      return source;
    }
    return main.replaceFirst("void polydium_glMain()")
      + "\nvoid main() {\n  polydium_glMain();\n  gl_Position.z = (gl_Position.z + gl_Position.w) * 0.5;\n}\n";
  }
  *///?}

  private IntermediaryShaderModule compileShader(final VulkanDevice.ShaderCompilationKey key, final ShaderSource shaderSource) {
    String source = ShineVulkan.shader(key.id, key.type, DistantHorizonsVulkan.shader(key.id, shaderSource.get(key.id, key.type)));
    if (source == null) {
      LOGGER.error("Couldn't find source for {} shader ({})", key.type, key.id);
      return IntermediaryShaderModule.INVALID;
    } else {
      String sourceWithDefines = GlslPreprocessor.injectDefines(RESERVED_SAMPLER.matcher(source).replaceAll("sampler_"), key.defines);
      //? if < 26.1 {
      /*if (key.type == ShaderType.VERTEX) {
        sourceWithDefines = glToVulkanDepth(sourceWithDefines);
      }
      *///?}

      try {
        return this.glslCompiler.createIntermediary(key.id.toDebugFileName(), sourceWithDefines, key.type);
      } catch (ShaderCompileException var6) {
        LOGGER.error("Couldn't compile {} shader {}: {}", new Object[]{key.type, key.id, var6.getMessage()});
        return IntermediaryShaderModule.INVALID;
      }
    }
  }

  private VulkanRenderPipeline compilePipeline(final RenderPipeline pipeline, final ShaderSource shaderSource) {
    IntermediaryShaderModule vertexShader = this.getOrCompileShader(pipeline.getVertexShader(), ShaderType.VERTEX, pipeline.getShaderDefines(), shaderSource);
    IntermediaryShaderModule fragmentShader = this.getOrCompileShader(
      pipeline.getFragmentShader(), ShaderType.FRAGMENT, pipeline.getShaderDefines(), shaderSource
    );
    if (vertexShader == IntermediaryShaderModule.INVALID) {
      LOGGER.error("Couldn't compile pipeline {}: vertex shader {} was invalid", pipeline.getLocation(), pipeline.getVertexShader());
      return new VulkanRenderPipeline(pipeline, this, 0L, 0L, 0L, VulkanBindGroupLayout.INVALID_LAYOUT, 0L, 0L);
    } else if (fragmentShader == IntermediaryShaderModule.INVALID) {
      LOGGER.error("Couldn't compile pipeline {}: fragment shader {} was invalid", pipeline.getLocation(), pipeline.getFragmentShader());
      return new VulkanRenderPipeline(pipeline, this, 0L, 0L, 0L, VulkanBindGroupLayout.INVALID_LAYOUT, 0L, 0L);
    } else {
      try {
        GlslCompiler.CompiledModules modules = this.glslCompiler.compile(this, pipeline, vertexShader, fragmentShader);
        return VulkanRenderPipeline.compile(this, modules.layout(), pipeline, modules.vertex(), modules.fragment(), modules.inputTypes());
      } catch (ShaderCompileException var6) {
        LOGGER.error("Couldn't compile pipeline {}: {}", pipeline.getLocation(), var6.getMessage());
        return new VulkanRenderPipeline(pipeline, this, 0L, 0L, 0L, VulkanBindGroupLayout.INVALID_LAYOUT, 0L, 0L);
      }
    }
  }

  @Override
  public void clearPipelineCache() {
    this.graphicsQueue.waitIdle();
    this.pipelineCache.values().forEach(VulkanRenderPipeline::destroy);
    this.pipelineCache.clear();
    this.shaderCache.values().forEach(IntermediaryShaderModule::close);
    this.shaderCache.clear();
  }

  public GpuQueryPool createTimestampQueryPool(final int size) {
    return new VulkanQueryPool(this, size);
  }

  public long getTimestampNow() {
    return this.commandEncoder.getTimestampNow();
  }

  public CheckpointExtension checkpointExtension() {
    return this.checkpointExtension;
  }

  private @Nullable GpuSurface surface;
  private long window;
  private boolean vsync;
  private boolean surfaceNeedsReconfiguring = true;

  public void initSurface(final long window) {
    this.window = window;
    this.surface = new GpuSurface(new VulkanGpuSurface(this, window));
  }

  @Override
  public void setVsync(final boolean enabled) {
    this.vsync = enabled;
    this.surfaceNeedsReconfiguring = true;
  }

  void presentTexture(final GpuTextureView texture) {
    int[] width = new int[1];
    int[] height = new int[1];
    GLFW.glfwGetFramebufferSize(this.window, width, height);
    if (width[0] == 0 || height[0] == 0) {
      return;
    }
    SodiumExtraMac.reduceSwapchain(this.window, width, height);

    GpuSurface.Configuration current = this.surface.currentConfiguration().orElse(null);
    if (this.surfaceNeedsReconfiguring || current == null || current.width() != width[0] || current.height() != height[0] || this.surface.isSuboptimal()) {
      GpuSurface.Configuration config = new GpuSurface.Configuration(
        width[0], height[0], GpuSurface.PresentMode.getSupportedVsyncMode(this.surface.supportedPresentModes(), this.vsync)
      );

      try {
        this.surface.configure(config);
        this.surfaceNeedsReconfiguring = false;
      } catch (SurfaceException e) {
        LOGGER.warn("Couldn't configure surface to {}: {}", config, e);
        return;
      }
    }

    try {
      this.surface.acquireNextTexture();
    } catch (SurfaceException e) {
      LOGGER.warn("Couldn't acquire next surface texture with config {}: {}", this.surface.currentConfiguration(), e);
      this.surfaceNeedsReconfiguring = true;
      return;
    }

    this.surface.blitFromTexture(this.commandEncoder, texture);
  }

  @Override
  public void presentFrame() {
    this.commandEncoder.submit();
    if (this.surface.isAcquired()) {
      this.surface.present();
    }
  }

  @Override
  public boolean isZZeroToOne() {
    return true;
  }

  @Override
  public String getImplementationInformation() {
    return this.deviceInfo.name() + " Vulkan " + this.deviceInfo.driverInfo() + ", " + this.deviceInfo.vendorName();
  }

  @Override
  public String getVendor() {
    return this.deviceInfo.vendorName();
  }

  @Override
  public String getBackendName() {
    return this.deviceInfo.backendName();
  }

  @Override
  public String getVersion() {
    return this.deviceInfo.driverInfo();
  }

  @Override
  public String getRenderer() {
    return this.deviceInfo.name();
  }

  @Override
  public int getMaxTextureSize() {
    return this.deviceInfo.limits().maxTextureSize();
  }

  @Override
  public int getUniformOffsetAlignment() {
    return this.deviceInfo.limits().minUniformOffsetAlignment();
  }

  @Override
  public List<String> getEnabledExtensions() {
    return List.copyOf(this.deviceInfo.underlyingExtensions());
  }

  @Override
  public int getMaxSupportedAnisotropy() {
    return this.deviceInfo.limits().maxAnisotropy();
  }

  private record ShaderCompilationKey(Identifier id, ShaderType type, ShaderDefines defines) {
    @Override
    public String toString() {
      String string = this.id + " (" + this.type + ")";
      return !this.defines.isEmpty() ? string + " with " + this.defines : string;
    }
  }
}
