package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.pipeline.BlendFunction;
import org.polyfrost.polydium.vulkan.compat.BlendFactor;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfoKHR;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo.Buffer;

public record VulkanRenderPipeline(
  RenderPipeline info,
  VulkanDevice device,
  long withDepthPipeline,
  long withoutDepthPipeline,
  long pipelineLayout,
  VulkanBindGroupLayout layout,
  long vertexModule,
  long fragmentModule
) implements CompiledRenderPipeline, Destroyable {
  public static final long INVALID_PIPELINE = 0L;
  public static final int SODIUM_PUSH_CONSTANT_RANGE = 20;

  @Override
  public boolean isValid() {
    return this.withDepthPipeline != 0L;
  }

  public static @Nullable DepthStencilState depthState(final RenderPipeline pipeline) {
    //? if >= 26.1 {
    return pipeline.getDepthStencilState();
    //?} else {
    /*com.mojang.blaze3d.platform.CompareOp op = switch (pipeline.getDepthTestFunction()) {
      case NO_DEPTH_TEST -> null;
      case EQUAL_DEPTH_TEST -> com.mojang.blaze3d.platform.CompareOp.EQUAL;
      case LEQUAL_DEPTH_TEST -> com.mojang.blaze3d.platform.CompareOp.LESS_THAN_OR_EQUAL;
      case LESS_DEPTH_TEST -> com.mojang.blaze3d.platform.CompareOp.LESS_THAN;
      case GREATER_DEPTH_TEST -> com.mojang.blaze3d.platform.CompareOp.GREATER_THAN;
    };
    return op == null ? null : new DepthStencilState(op, pipeline.isWriteDepth(), pipeline.getDepthBiasScaleFactor(), pipeline.getDepthBiasConstant());
    *///?}
  }

  public static ColorTargetState colorState(final RenderPipeline pipeline) {
    //? if >= 26.1 {
    return pipeline.getColorTargetState();
    //?} else {
    /*java.util.Optional<BlendFunction> blend = pipeline.getColorLogic() == com.mojang.blaze3d.platform.LogicOp.OR_REVERSE
      ? java.util.Optional.of(BlendFunction.INVERT)
      : pipeline.getBlendFunction();
    int writeMask = (pipeline.isWriteColor() ? ColorTargetState.WRITE_COLOR : 0) | (pipeline.isWriteAlpha() ? ColorTargetState.WRITE_ALPHA : 0);
    return new ColorTargetState(blend, writeMask);
    *///?}
  }

  public static RenderPipeline.Builder withDepth(final RenderPipeline.Builder builder, final @Nullable DepthStencilState depth) {
    //? if >= 26.1 {
    return builder.withDepthStencilState(java.util.Optional.ofNullable(depth));
    //?} else {
    /*if (depth == null) {
      return builder.withDepthTestFunction(com.mojang.blaze3d.platform.DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false);
    }
    builder.withDepthTestFunction(switch (depth.depthTest()) {
      case EQUAL -> com.mojang.blaze3d.platform.DepthTestFunction.EQUAL_DEPTH_TEST;
      case LESS_THAN_OR_EQUAL -> com.mojang.blaze3d.platform.DepthTestFunction.LEQUAL_DEPTH_TEST;
      case LESS_THAN -> com.mojang.blaze3d.platform.DepthTestFunction.LESS_DEPTH_TEST;
      case GREATER_THAN -> com.mojang.blaze3d.platform.DepthTestFunction.GREATER_DEPTH_TEST;
      default -> throw new IllegalArgumentException("1.21.11 pipelines have no depth test " + depth.depthTest());
    }).withDepthWrite(depth.writeDepth());
    if (depth.depthBiasScaleFactor() != 0.0F || depth.depthBiasConstant() != 0.0F) {
      builder.withDepthBias(depth.depthBiasScaleFactor(), depth.depthBiasConstant());
    }
    return builder;
    *///?}
  }

  public static RenderPipeline.Builder withColor(final RenderPipeline.Builder builder, final ColorTargetState color) {
    //? if >= 26.1 {
    return builder.withColorTargetState(color);
    //?} else {
    /*if (color.blendFunction().isPresent()) {
      builder.withBlend(color.blendFunction().get());
    } else {
      builder.withoutBlend();
    }
    return builder.withColorWrite(color.writeRed(), color.writeAlpha());
    *///?}
  }

  public static VulkanRenderPipeline compile(
    final VulkanDevice device, final VulkanBindGroupLayout layout, final RenderPipeline pipeline, final long vertexModule, final long fragmentModule,
    final java.util.Map<String, Integer> inputTypes
  ) {
    MemoryStack stack = MemoryStack.stackPush();

    long pipelineLayout;
    try {
      VkPipelineLayoutCreateInfo createInfo = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(stack.longs(layout.handle()));
      if (pipeline.getLocation().getNamespace().equals("sodium")) {
        createInfo.pPushConstantRanges(org.lwjgl.vulkan.VkPushConstantRange.calloc(1, stack).offset(0).size(SODIUM_PUSH_CONSTANT_RANGE).stageFlags(VK12.VK_SHADER_STAGE_ALL));
      }
      LongBuffer pointer = stack.callocLong(1);
      VulkanUtils.crashIfFailure(
        device, VK12.vkCreatePipelineLayout(device.vkDevice(), createInfo, null, pointer), "Can't create pipeline for " + pipeline.getLocation()
      );
      pipelineLayout = pointer.get(0);
      device.instance().debug().setObjectName(device.vkDevice(), 17, pipelineLayout, () -> "Pipeline layout for " + pipeline.getLocation());
    } catch (Throwable var41) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var40) {
          var41.addSuppressed(var40);
        }
      }

      throw var41;
    }

    if (stack != null) {
      stack.close();
    }

    stack = MemoryStack.stackPush();

    VulkanRenderPipeline var38;
    try {
      Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
      ByteBuffer nameMain = stack.UTF8("main");
      VkPipelineShaderStageCreateInfo vertexStage = VkPipelineShaderStageCreateInfo.calloc(stack).sType$Default().stage(1).module(vertexModule).pName(nameMain);
      VkPipelineShaderStageCreateInfo fragmentStage = VkPipelineShaderStageCreateInfo.calloc(stack)
        .sType$Default()
        .stage(16)
        .module(fragmentModule)
        .pName(nameMain);
      ((Buffer)((Buffer)shaderStages.put(vertexStage)).put(fragmentStage)).flip();
      VertexFormat vertexFormat = pipeline.getVertexFormat();
      org.lwjgl.vulkan.VkVertexInputAttributeDescription.Buffer vertexAttributeDescriptions = VkVertexInputAttributeDescription.calloc(
        vertexFormat.getElements().size(), stack
      );
      org.lwjgl.vulkan.VkVertexInputBindingDescription.Buffer vertexBindingDescriptions = VkVertexInputBindingDescription.calloc(1, stack)
        .binding(0)
        .stride(vertexFormat.getVertexSize())
        .inputRate(0);
      int attribLocation = 0;
      java.util.List<String> attribNames = vertexFormat.getElementAttributeNames();

      for (VertexFormatElement element : vertexFormat.getElements()) {
        vertexAttributeDescriptions.get(attribLocation)
          .location(attribLocation)
          .binding(0)
          .offset(vertexFormat.getOffset(element))
          .format(VulkanConst.toVk(element, inputTypes.getOrDefault(attribNames.get(attribLocation), 0)));
        attribLocation++;
      }

      VkPipelineVertexInputStateCreateInfo vertexInputState = VkPipelineVertexInputStateCreateInfo.calloc(stack)
        .sType$Default()
        .pVertexAttributeDescriptions(vertexAttributeDescriptions)
        .pVertexBindingDescriptions(vertexBindingDescriptions);
      VkPipelineInputAssemblyStateCreateInfo inputAssemblyState = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
        .sType$Default()
        .topology(VulkanConst.toVk(pipeline.getVertexFormatMode()));
      VkPipelineRasterizationStateCreateInfo rasterizationState = VkPipelineRasterizationStateCreateInfo.calloc(stack)
        .sType$Default()
        .polygonMode(device.fillModeNonSolid() ? VulkanConst.toVk(pipeline.getPolygonMode()) : 0)
        .cullMode(pipeline.isCull() ? 2 : 0)
        .frontFace(MoulberryVulkan.cullFront(pipeline) ? 0 : 1)
        .lineWidth(1.0F);
      VkPipelineDepthStencilStateCreateInfo depthStencilState = VkPipelineDepthStencilStateCreateInfo.calloc(stack).sType$Default();
      DepthStencilState depth = depthState(pipeline);
      if (depth != null) {
        rasterizationState.depthBiasEnable(depth.depthBiasConstant() != 0.0F || depth.depthBiasScaleFactor() != 0.0F);
        rasterizationState.depthBiasConstantFactor(depth.depthBiasConstant());
        rasterizationState.depthBiasSlopeFactor(depth.depthBiasScaleFactor());
        depthStencilState.depthTestEnable(true);
        depthStencilState.depthWriteEnable(depth.writeDepth());
        depthStencilState.depthCompareOp(VulkanConst.toVk(depth.depthTest()));
      }

      ColorTargetState[] colorTargetStates = VulkanActive.COLOR_TARGETS.getOrDefault(pipeline, new ColorTargetState[]{colorState(pipeline)});
      org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState.Buffer blendAttachments = VkPipelineColorBlendAttachmentState.calloc(colorTargetStates.length, stack);
      IntBuffer colorAttachmentFormats = stack.mallocInt(colorTargetStates.length);
      for (int i = 0; i < colorTargetStates.length; i++) {
        blendAttachments.position(i);
        blendAttachments.colorWriteMask(VulkanConst.toVk(colorTargetStates[i]));
        if (colorTargetStates[i].blendFunction().isPresent()) {
          applyBlendInformation(blendAttachments, colorTargetStates[i].blendFunction().get());
        }
        colorAttachmentFormats.put(i, VulkanConst.toVk(MoulberryVulkan.colorFormat(pipeline)));
      }
      blendAttachments.position(0);

      VkPipelineColorBlendStateCreateInfo colorBlendState = VkPipelineColorBlendStateCreateInfo.calloc(stack).sType$Default().pAttachments(blendAttachments);
      VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack).sType$Default().scissorCount(1).viewportCount(1);
      VkPipelineMultisampleStateCreateInfo multisampleState = VkPipelineMultisampleStateCreateInfo.calloc(stack)
        .sType$Default()
        .rasterizationSamples(1)
        .sampleShadingEnable(false);
      VkPipelineDynamicStateCreateInfo dynamicStateInfo = VkPipelineDynamicStateCreateInfo.calloc(stack).sType$Default().pDynamicStates(stack.ints(1, 0));
      VkPipelineRenderingCreateInfoKHR renderingInfo = VkPipelineRenderingCreateInfoKHR.calloc(stack).sType$Default();
      renderingInfo.pColorAttachmentFormats(colorAttachmentFormats);
      renderingInfo.depthAttachmentFormat(126);
      org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo.Buffer createInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
        .sType$Default()
        .flags(0)
        .pStages(shaderStages)
        .pVertexInputState(vertexInputState)
        .pInputAssemblyState(inputAssemblyState)
        .pRasterizationState(rasterizationState)
        .pDepthStencilState(depthStencilState)
        .pColorBlendState(colorBlendState)
        .pViewportState(viewportState)
        .pMultisampleState(multisampleState)
        .pDynamicState(dynamicStateInfo)
        .layout(pipelineLayout)
        .pNext(renderingInfo);
      LongBuffer pointer = stack.callocLong(1);
      VulkanUtils.crashIfFailure(
        device, VK12.vkCreateGraphicsPipelines(device.vkDevice(), 0L, createInfo, null, pointer), "Can't compile pipeline " + pipeline.getLocation()
      );
      long withDepthPipeline = pointer.get(0);
      device.instance().debug().setObjectName(device.vkDevice(), 19, withDepthPipeline, () -> "Pipeline " + pipeline.getLocation());
      long withoutDepthPipeline;
      if (depth == null) {
        renderingInfo.depthAttachmentFormat(0);
        VulkanUtils.crashIfFailure(
          device, VK12.vkCreateGraphicsPipelines(device.vkDevice(), 0L, createInfo, null, pointer), "Can't compile pipeline " + pipeline.getLocation()
        );
        withoutDepthPipeline = pointer.get(0);
        device.instance().debug().setObjectName(device.vkDevice(), 19, withoutDepthPipeline, () -> "Pipeline " + pipeline.getLocation());
      } else {
        withoutDepthPipeline = 0L;
      }

      var38 = new VulkanRenderPipeline(pipeline, device, withDepthPipeline, withoutDepthPipeline, pipelineLayout, layout, vertexModule, fragmentModule);
    } catch (Throwable var42) {
      if (stack != null) {
        try {
          stack.close();
        } catch (Throwable var39) {
          var42.addSuppressed(var39);
        }
      }

      throw var42;
    }

    if (stack != null) {
      stack.close();
    }

    return var38;
  }

  @Override
  public void destroy() {
    if (this.withDepthPipeline != 0L) {
      VK12.vkDestroyPipeline(this.device.vkDevice(), this.withoutDepthPipeline, null);
      VK12.vkDestroyPipeline(this.device.vkDevice(), this.withDepthPipeline, null);
      VK12.vkDestroyPipelineLayout(this.device.vkDevice(), this.pipelineLayout, null);
      VK12.vkDestroyDescriptorSetLayout(this.device.vkDevice(), this.layout.handle(), null);
      VK12.vkDestroyShaderModule(this.device.vkDevice(), this.vertexModule, null);
      VK12.vkDestroyShaderModule(this.device.vkDevice(), this.fragmentModule, null);
    }
  }

  private static void applyBlendInformation(
    final org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState.Buffer blendAttachments, final BlendFunction blendFunction
  ) {
    blendAttachments.blendEnable(true)
      .colorBlendOp(0)
      .alphaBlendOp(0)
      .dstAlphaBlendFactor(VulkanConst.toVk(BlendFactor.valueOf(blendFunction.destAlpha().name())))
      .dstColorBlendFactor(VulkanConst.toVk(BlendFactor.valueOf(blendFunction.destColor().name())))
      .srcAlphaBlendFactor(VulkanConst.toVk(BlendFactor.valueOf(blendFunction.sourceAlpha().name())))
      .srcColorBlendFactor(VulkanConst.toVk(BlendFactor.valueOf(blendFunction.sourceColor().name())));
  }
}
