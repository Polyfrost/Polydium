package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.polyfrost.polydium.client.perf.VulkanPipelineCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.LongBuffer;

@Mixin(VulkanRenderPipeline.class)
public abstract class MixinVulkanPipelineCacheUse {
    @WrapOperation(
        method = "compile",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCreateGraphicsPipelines(Lorg/lwjgl/vulkan/VkDevice;JLorg/lwjgl/vulkan/VkGraphicsPipelineCreateInfo$Buffer;Lorg/lwjgl/vulkan/VkAllocationCallbacks;Ljava/nio/LongBuffer;)I"),
        require = 2,
        allow = 2
    )
    private static int polydium$usePipelineCache(VkDevice vkDevice, long pipelineCache, VkGraphicsPipelineCreateInfo.Buffer createInfos, VkAllocationCallbacks allocator, LongBuffer pipelines, Operation<Integer> original, @Local(argsOnly = true) VulkanDevice device) {
        return ((VulkanPipelineCache.Holder) device).polydium$pipelineCache().createPipelines(vkDevice, createInfos, allocator, pipelines, original);
    }
}
//?}
