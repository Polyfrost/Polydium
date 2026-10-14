package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.polyfrost.polydium.client.perf.TexelViewCache;
import org.polyfrost.polydium.vulkan.Destroyable;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanGpuBuffer;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.LongBuffer;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanRenderPassTexelViewBackport {
    @Unique
    private boolean polydium$texelViewCached;

    @WrapOperation(
            method = "pushDescriptors()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/vulkan/VK12;vkCreateBufferView(Lorg/lwjgl/vulkan/VkDevice;Lorg/lwjgl/vulkan/VkBufferViewCreateInfo;Lorg/lwjgl/vulkan/VkAllocationCallbacks;Ljava/nio/LongBuffer;)I"
            )
    )
    private int polydium$reuseTexelView(VkDevice device, VkBufferViewCreateInfo info, VkAllocationCallbacks allocator, LongBuffer view, Operation<Integer> original, @Local GpuBufferSlice slice) {
        this.polydium$texelViewCached = false;
        if (!(slice.buffer() instanceof VulkanGpuBuffer.Direct direct) || direct.isClosed()) {
            return original.call(device, info, allocator, view);
        }
        TexelViewCache.Views views = ((TexelViewCache) direct).polydium$texelViews();
        if (views.reuse(info, view)) {
            this.polydium$texelViewCached = true;
            return VK10.VK_SUCCESS;
        }

        int result = original.call(device, info, allocator, view);
        if (result == VK10.VK_SUCCESS) {
            this.polydium$texelViewCached = views.store(info, view.get(0));
        }
        return result;
    }

    @WrapWithCondition(
            method = "pushDescriptors()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/polyfrost/polydium/vulkan/VulkanCommandEncoder;queueForDestroy(Lorg/polyfrost/polydium/vulkan/Destroyable;)V"
            )
    )
    private boolean polydium$keepCachedTexelView(VulkanCommandEncoder encoder, Destroyable destroyable) {
        return !this.polydium$texelViewCached;
    }
}
