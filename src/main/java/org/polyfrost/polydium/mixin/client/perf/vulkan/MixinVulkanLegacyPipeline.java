package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
//? if < 26.3
/*import com.mojang.blaze3d.vulkan.VulkanBindGroupLayout;*/
import org.lwjgl.vulkan.VkAllocationCallbacks;
//? if >= 26.3
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfoKHR;
import org.polyfrost.polydium.client.perf.VulkanLegacyRendering;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.LongBuffer;

@Mixin(VulkanRenderPipeline.class)
public abstract class MixinVulkanLegacyPipeline implements VulkanLegacyRendering.Pipeline {
    //? if >= 26.3 {
    @Shadow
    @Final
    private long descriptorSetLayout;

    @Override
    public long polydium$descriptorSetLayout() {
        return this.descriptorSetLayout;
    }

    @WrapOperation(method = "compile", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkDescriptorSetLayoutCreateInfo;flags(I)Lorg/lwjgl/vulkan/VkDescriptorSetLayoutCreateInfo;"))
    private static VkDescriptorSetLayoutCreateInfo polydium$plainSetLayout(VkDescriptorSetLayoutCreateInfo createInfo, int flags, Operation<VkDescriptorSetLayoutCreateInfo> original, @Local(argsOnly = true) VulkanDevice device) {
        return original.call(createInfo, ((VulkanLegacyRendering.Device) device).polydium$pushDescriptors() ? flags : 0);
    }
    //?} else {
    /*@Shadow
    public abstract VulkanBindGroupLayout layout();

    @Override
    public long polydium$descriptorSetLayout() {
        return this.layout().handle();
    }
    *///?}

    // Without dynamic rendering, compile against a render pass built from the VkPipelineRenderingCreateInfo vanilla chains
    @WrapOperation(
        method = "compile",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCreateGraphicsPipelines(Lorg/lwjgl/vulkan/VkDevice;JLorg/lwjgl/vulkan/VkGraphicsPipelineCreateInfo$Buffer;Lorg/lwjgl/vulkan/VkAllocationCallbacks;Ljava/nio/LongBuffer;)I"),
        require = 2,
        allow = 2
    )
    private static int polydium$useRenderPass(VkDevice vkDevice, long pipelineCache, VkGraphicsPipelineCreateInfo.Buffer createInfos, VkAllocationCallbacks allocator, LongBuffer pipelines, Operation<Integer> original, @Local(argsOnly = true) VulkanDevice device) {
        VulkanLegacyRendering.Device legacy = (VulkanLegacyRendering.Device) device;
        if (legacy.polydium$dynamicRendering()) {
            return original.call(vkDevice, pipelineCache, createInfos, allocator, pipelines);
        }

        VkGraphicsPipelineCreateInfo createInfo = createInfos.get(0);
        long renderingInfo = createInfo.pNext();
        createInfo.pNext(0L).renderPass(legacy.polydium$renderPassCache().get(VkPipelineRenderingCreateInfoKHR.create(renderingInfo))).subpass(0);
        try {
            return original.call(vkDevice, pipelineCache, createInfos, allocator, pipelines);
        } finally {
            // Vanilla edits the chained struct for the depthless variant and creates again
            createInfo.pNext(renderingInfo);
        }
    }
}
//?}
