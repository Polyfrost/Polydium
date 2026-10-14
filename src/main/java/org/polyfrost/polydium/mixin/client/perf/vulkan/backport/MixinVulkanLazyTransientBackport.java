package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanFrontSubmission;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanTransientMemory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanTransientMemory.class)
public abstract class MixinVulkanLazyTransientBackport {
    @Shadow
    @Final
    private VulkanDevice device;

    @Shadow
    @Final
    private VulkanCommandEncoder encoder;

    @Shadow
    private VkCommandBuffer commandBuffer;

    @WrapOperation(method = "beginSubmit", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanCommandEncoder;allocateAndBeginTransientCommandBuffer()Lorg/lwjgl/vulkan/VkCommandBuffer;"))
    private VkCommandBuffer polydium$beginLazily(VulkanCommandEncoder encoder, Operation<VkCommandBuffer> original) {
        if (this.commandBuffer != null) {
            throw new IllegalStateException("Transient command buffer is still open");
        }
        return null;
    }

    @WrapWithCondition(method = "beginSubmit", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanCommandEncoder;execute(Lorg/lwjgl/vulkan/VkCommandBuffer;)V"))
    private boolean polydium$executeOnFirstCopy(VulkanCommandEncoder encoder, VkCommandBuffer commandBuffer) {
        return commandBuffer != null;
    }

    @Inject(method = "recordGpuMappedCopy", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkBufferCopy;calloc(ILorg/lwjgl/system/MemoryStack;)Lorg/lwjgl/vulkan/VkBufferCopy$Buffer;"))
    private void polydium$beginOnFirstCopy(CallbackInfo ci) {
        if (this.commandBuffer != null) {
            return;
        }
        this.commandBuffer = this.encoder.allocateAndBeginTransientCommandBuffer();
        ((VulkanFrontSubmission) ((MixinVulkanLazyTransientEncoderAccessorBackport) this.encoder).polydium$submissionBuilder()).polydium$executeFirst(this.commandBuffer, this.device.graphicsQueue());
    }

    @WrapOperation(method = "endSubmit", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkEndCommandBuffer(Lorg/lwjgl/vulkan/VkCommandBuffer;)I"))
    private int polydium$endOnlyStarted(VkCommandBuffer commandBuffer, Operation<Integer> original) {
        if (commandBuffer == null) {
            return VK10.VK_SUCCESS;
        }
        return original.call(commandBuffer);
    }
}
