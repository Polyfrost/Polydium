package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanTransientMemory;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanFrontSubmission;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanTransientMemory.class)
public abstract class MixinVulkanLazyTransient {
    @Shadow
    @Final
    private VulkanDevice device;

    @Shadow
    @Final
    private VulkanCommandEncoder encoder;

    @Shadow
    private boolean anyCommandRecorded;

    @Shadow
    private VkCommandBuffer commandBuffer;

    @Inject(method = "beginSubmit", at = @At("HEAD"), cancellable = true)
    private void polydium$beginLazily(CallbackInfo ci) {
        if (this.commandBuffer != null) {
            throw new IllegalStateException("Transient command buffer is still open");
        }

        this.anyCommandRecorded = false;
        ci.cancel();
    }

    @Inject(method = "recordGpuMappedCopy", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkBufferCopy;calloc(ILorg/lwjgl/system/MemoryStack;)Lorg/lwjgl/vulkan/VkBufferCopy$Buffer;"))
    private void polydium$beginOnFirstCopy(CallbackInfo ci) {
        if (this.commandBuffer != null) {
            return;
        }

        this.commandBuffer = this.encoder.allocateAndBeginTransientCommandBuffer();
        ((VulkanFrontSubmission) ((MixinVulkanLazyTransientEncoderAccessor) this.encoder).polydium$submissionBuilder()).polydium$executeFirst(this.commandBuffer, this.device.graphicsQueue());
    }

    @WrapOperation(method = "endSubmit", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkEndCommandBuffer(Lorg/lwjgl/vulkan/VkCommandBuffer;)I"))
    private int polydium$endOnlyStarted(VkCommandBuffer commandBuffer, Operation<Integer> original) {
        if (commandBuffer == null) {
            return VK10.VK_SUCCESS;
        }

        return original.call(commandBuffer);
    }
}
//?}
