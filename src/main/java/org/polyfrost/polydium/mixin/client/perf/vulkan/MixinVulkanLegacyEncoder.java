package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.polyfrost.polydium.client.perf.VulkanLegacyRendering;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanLegacyEncoder implements VulkanLegacyRendering.Encoder {
    @Shadow
    @Final
    private VulkanDevice device;

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
        if (((VulkanLegacyRendering.Device) this.device).polydium$dynamicRendering()) {
            original.call(commandBuffer, renderingInfo);
        } else {
            VulkanLegacyRendering.beginRenderPass(this.device, (VulkanCommandEncoder) (Object) this, commandBuffer, descriptor, renderingInfo.renderArea());
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

    @Inject(method = "destroy", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkDestroySemaphore(Lorg/lwjgl/vulkan/VkDevice;JLorg/lwjgl/vulkan/VkAllocationCallbacks;)V"))
    private void polydium$destroyDescriptorPools(CallbackInfo ci) {
        if (this.polydium$descriptorPools != null) {
            this.polydium$descriptorPools.destroy();
        }
    }
}
//?}
