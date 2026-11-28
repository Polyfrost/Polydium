package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.polyfrost.polydium.client.perf.VulkanLegacyRendering;
import org.polyfrost.polydium.client.perf.VulkanSync;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfileFlag;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanLegacyEncoder implements VulkanLegacyRendering.Encoder {
    @Unique
    private static final Logger polydium$LOGGER = LogUtils.getLogger();

    @Shadow
    @Final
    private VulkanDevice device;

    @Shadow
    private @Nullable VkCommandBuffer currentCommandBuffer;

    @Shadow
    private @Nullable VulkanRenderPass currentRenderPass;

    //? if >= 26.3 {
    @Shadow
    private @Nullable VkCommandBuffer currentInitCommandBuffer;
    //?}

    @Shadow
    private void endCommandBuffer() {
        throw new AssertionError();
    }

    @Unique
    private VulkanLegacyRendering.DescriptorPools polydium$descriptorPools;

    @Override
    public long polydium$allocateDescriptorSet(long setLayout) {
        if (this.polydium$descriptorPools == null) {
            this.polydium$descriptorPools = new VulkanLegacyRendering.DescriptorPools(this.device, (VulkanCommandEncoder) (Object) this);
        }
        return this.polydium$descriptorPools.allocate(setLayout);
    }

    @WrapOperation(
        method = "createRenderPass",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRDynamicRendering;vkCmdBeginRenderingKHR(Lorg/lwjgl/vulkan/VkCommandBuffer;Lorg/lwjgl/vulkan/VkRenderingInfo;)V")
    )
    private void polydium$beginRenderPass(VkCommandBuffer commandBuffer, VkRenderingInfo renderingInfo, Operation<Void> original, @Local(argsOnly = true) RenderPassDescriptor descriptor) {
        boolean profiled = GpuProfileFlag.ENABLED && GpuProfiler.recording;
        if (profiled) {
            GpuProfiler.beginPass(commandBuffer);
        }
        if (((VulkanLegacyRendering.Device) this.device).polydium$dynamicRendering()) {
            original.call(commandBuffer, renderingInfo);
        } else {
            VulkanLegacyRendering.beginRenderPass(this.device, (VulkanCommandEncoder) (Object) this, commandBuffer, descriptor, renderingInfo.renderArea());
        }
        if (profiled) {
            GpuProfiler.end(commandBuffer);
        }
    }

    @WrapOperation(method = "submitRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRDynamicRendering;vkCmdEndRenderingKHR(Lorg/lwjgl/vulkan/VkCommandBuffer;)V"))
    private void polydium$endRenderPass(VkCommandBuffer commandBuffer, Operation<Void> original) {
        if (((VulkanLegacyRendering.Device) this.device).polydium$dynamicRendering()) {
            original.call(commandBuffer);
        } else {
            VulkanLegacyRendering.endRenderPass(commandBuffer);
        }
    }

    @Inject(method = "submit", at = @At("HEAD"))
    private void polydium$retireDescriptorPool(CallbackInfo ci) {
        if (this.polydium$descriptorPools != null) {
            this.polydium$descriptorPools.retire();
        }
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void polydium$endOpenCommandBuffer(CallbackInfo ci) {
        //? if >= 26.3 {
        boolean open = this.currentCommandBuffer != null || this.currentInitCommandBuffer != null;
        //?} else
        /*boolean open = this.currentCommandBuffer != null;*/
        if (open && this.currentRenderPass == null) {
            polydium$LOGGER.warn("Ending open Vulkan command buffer before device shutdown");
            this.endCommandBuffer();
        }
    }

    @Inject(method = "destroy", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkDestroySemaphore(Lorg/lwjgl/vulkan/VkDevice;JLorg/lwjgl/vulkan/VkAllocationCallbacks;)V"))
    private void polydium$destroyDescriptorPools(CallbackInfo ci) {
        if (this.polydium$descriptorPools != null) {
            this.polydium$descriptorPools.destroy();
        }
    }

    @WrapOperation(
        method = "memoryBarrier(Lorg/lwjgl/vulkan/VkCommandBuffer;Lorg/lwjgl/system/MemoryStack;)V",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRSynchronization2;vkCmdPipelineBarrier2KHR(Lorg/lwjgl/vulkan/VkCommandBuffer;Lorg/lwjgl/vulkan/VkDependencyInfo;)V")
    )
    private static void polydium$legacyBarrier(VkCommandBuffer commandBuffer, VkDependencyInfo info, Operation<Void> original) {
        if (VulkanSync.sync2) {
            original.call(commandBuffer, info);
        } else {
            VulkanSync.pipelineBarrier(commandBuffer, info);
        }
    }

    @WrapOperation(
        method = {"writeTimestamp", "getTimestampNow"},
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRSynchronization2;vkCmdWriteTimestamp2KHR(Lorg/lwjgl/vulkan/VkCommandBuffer;JJI)V"),
        require = 2
    )
    private void polydium$legacyTimestamp(VkCommandBuffer commandBuffer, long stage, long queryPool, int query, Operation<Void> original) {
        if (VulkanSync.sync2) {
            original.call(commandBuffer, stage, queryPool, query);
        } else {
            VulkanSync.writeTimestamp(commandBuffer, stage, queryPool, query);
        }
    }
}
//?}
