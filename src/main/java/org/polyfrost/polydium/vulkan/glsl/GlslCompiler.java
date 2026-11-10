package org.polyfrost.polydium.vulkan.glsl;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderType;
import org.polyfrost.polydium.vulkan.VulkanBindGroupLayout;
import org.polyfrost.polydium.vulkan.VulkanConst;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.compat.GpuFormat;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.renderer.ShaderDefines;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;

public class GlslCompiler implements AutoCloseable {
  private final long shaderCompiler = Shaderc.shaderc_compiler_initialize();
  private final long shaderOptions = Shaderc.shaderc_compile_options_initialize();
  private final ShaderDefines globalDefines;

  public GlslCompiler() {
    Shaderc.shaderc_compile_options_set_target_env(this.shaderOptions, 0, 4202496);
    Shaderc.shaderc_compile_options_set_auto_bind_uniforms(this.shaderOptions, true);
    Shaderc.shaderc_compile_options_set_auto_map_locations(this.shaderOptions, true);
    Shaderc.shaderc_compile_options_set_generate_debug_info(this.shaderOptions);
    Shaderc.shaderc_compile_options_set_optimization_level(this.shaderOptions, 0);
    this.globalDefines = ShaderDefines.builder().define("gl_VertexID", "gl_VertexIndex").define("gl_InstanceID", "gl_InstanceIndex").build();
  }

  public IntermediaryShaderModule createIntermediary(final String filename, String source, final ShaderType type) throws ShaderCompileException {
    source = GlslPreprocessor.injectDefines(source, this.globalDefines);
    int shaderType = type == ShaderType.FRAGMENT ? 1 : 0;
    ByteBuffer sourceBuffer = MemoryUtil.memUTF8(source, false);
    ByteBuffer filenameBuffer = MemoryUtil.memUTF8(filename);
    ByteBuffer entrypointBuffer = MemoryUtil.memUTF8("main");
    long result = Shaderc.shaderc_compile_into_spv(this.shaderCompiler, sourceBuffer, shaderType, filenameBuffer, entrypointBuffer, this.shaderOptions);

    IntermediaryShaderModule var13;
    try {
      int status = Shaderc.shaderc_result_get_compilation_status(result);
      if (status != 0) {
        throw new ShaderCompileException("Couldn't parse GLSL: " + Shaderc.shaderc_result_get_error_message(result));
      }

      ByteBuffer spirv = Shaderc.shaderc_result_get_bytes(result);
      ByteBuffer copy = MemoryUtil.memCalloc(spirv.remaining());
      MemoryUtil.memCopy(spirv, copy);
      var13 = IntermediaryShaderModule.createFromSpirv(filename, copy, type == ShaderType.VERTEX);
    } finally {
      Shaderc.shaderc_result_release(result);
      MemoryUtil.memFree(entrypointBuffer);
      MemoryUtil.memFree(filenameBuffer);
      MemoryUtil.memFree(sourceBuffer);
    }

    return var13;
  }

  public GlslCompiler.CompiledModules compile(
    final VulkanDevice device, final RenderPipeline pipeline, final IntermediaryShaderModule vertex, final IntermediaryShaderModule fragment
  ) throws ShaderCompileException {
    String pipelineName = pipeline.getLocation().toString();
    List<VulkanBindGroupLayout.Entry> entries = new ArrayList<>();
    addToBindGroup(entries, vertex, pipeline);
    addToBindGroup(entries, fragment, pipeline);
    List<String> vertexOutputNames = new ArrayList<>();

    for (SpvVariable output : vertex.outputs()) {
      vertexOutputNames.add(output.name());
    }

    List<String> vertexInputNames = new ArrayList<>();

    vertexInputNames.addAll(pipeline.getVertexFormat().getElementAttributeNames());

    vertex.rebind(vertexInputNames, entries, false);
    fragment.rebind(vertexOutputNames, entries, true);
    long vertexId = vertex.createVulkanShaderModule(device);
    long fragmentId = fragment.createVulkanShaderModule(device);
    VulkanBindGroupLayout layout = VulkanBindGroupLayout.create(device, entries, pipelineName);
    Map<String, Integer> inputTypes = new HashMap<>();
    for (SpvVariable input : vertex.inputs()) inputTypes.put(input.name(), input.baseType());
    return new GlslCompiler.CompiledModules(vertexId, fragmentId, layout, inputTypes);
  }

  @Override
  public void close() {
    Shaderc.shaderc_compile_options_release(this.shaderOptions);
    Shaderc.shaderc_compiler_release(this.shaderCompiler);
  }

  private static void addToBindGroup(final List<VulkanBindGroupLayout.Entry> entries, final IntermediaryShaderModule shader, final RenderPipeline pipeline) throws ShaderCompileException {
    for (SpvUniformBuffer buffer : shader.uniformBuffers()) {
      String name = buffer.name();
      if (entries.stream().noneMatch(e -> e.type() == VulkanBindGroupLayout.VulkanBindGroupEntryType.UNIFORM_BUFFER && e.name().equals(name))) {
        entries.add(new VulkanBindGroupLayout.Entry(VulkanBindGroupLayout.VulkanBindGroupEntryType.UNIFORM_BUFFER, name, null));
      }
    }

    for (SpvSampler sampler : shader.samplers()) {
      String namex = sampler.name();
      Optional<RenderPipeline.UniformDescription> uniformDescriptionx = pipeline.getUniforms().stream()
        .filter(d -> d.name().equals(namex))
        .findFirst();
      if (uniformDescriptionx.isPresent()) {
        if (sampler.dimensions() != 5) {
          throw new ShaderCompileException("UTB (" + namex + ") must have type of SpvDimBuffer");
        }

        if (entries.stream().noneMatch(e -> e.type() == VulkanBindGroupLayout.VulkanBindGroupEntryType.TEXEL_BUFFER && e.name().equals(namex))) {
          entries.add(
            new VulkanBindGroupLayout.Entry(VulkanBindGroupLayout.VulkanBindGroupEntryType.TEXEL_BUFFER, namex, texelFormat(pipeline, uniformDescriptionx.get()))
          );
        }
      } else {
        if (sampler.dimensions() != 1 && sampler.dimensions() != 3) {
          throw new ShaderCompileException("Sampled texture (" + namex + ") must have type of SpvDim2D or SpvDimCube");
        }

        if (entries.stream().noneMatch(e -> e.type() == VulkanBindGroupLayout.VulkanBindGroupEntryType.SAMPLED_IMAGE && e.name().equals(namex))) {
          entries.add(new VulkanBindGroupLayout.Entry(VulkanBindGroupLayout.VulkanBindGroupEntryType.SAMPLED_IMAGE, namex, null));
        }
      }
    }
  }

  private static GpuFormat texelFormat(final RenderPipeline pipeline, final RenderPipeline.UniformDescription uniform) {
    return pipeline.getLocation().getNamespace().equals("sodium")
      ? GpuFormat.R32_SINT
      : VulkanConst.toGpuFormat(uniform.textureFormat());
  }

  public record CompiledModules(long vertex, long fragment, VulkanBindGroupLayout layout, Map<String, Integer> inputTypes) {
  }
}
