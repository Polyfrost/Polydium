package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import org.polyfrost.polydium.client.perf.VulkanLegacyRendering;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanLegacyDescriptors {
    @Shadow
    @Final
    private VulkanDevice device;

    @Shadow
    @Final
    private VulkanCommandEncoder encoder;

    @Shadow
    protected VulkanRenderPipeline pipeline;

    // Without VK_KHR_push_descriptor, write a fresh set from the encoder's per-submit pool and bind it
    @WrapOperation(
        method = "pushDescriptors()V",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRPushDescriptor;vkCmdPushDescriptorSetKHR(Lorg/lwjgl/vulkan/VkCommandBuffer;IJILorg/lwjgl/vulkan/VkWriteDescriptorSet$Buffer;)V")
    )
    private void polydium$bindDescriptorSet(VkCommandBuffer commandBuffer, int bindPoint, long layout, int set, VkWriteDescriptorSet.Buffer writes, Operation<Void> original) {
        if (((VulkanLegacyRendering.Device) this.device).polydium$pushDescriptors()) {
            original.call(commandBuffer, bindPoint, layout, set, writes);
            return;
        }

        long descriptorSet = ((VulkanLegacyRendering.Encoder) this.encoder).polydium$allocateDescriptorSet(((VulkanLegacyRendering.Pipeline) (Object) this.pipeline).polydium$descriptorSetLayout());
        for (int i = writes.position(); i < writes.limit(); i++) {
            writes.get(i).dstSet(descriptorSet);
        }
        VK12.vkUpdateDescriptorSets(this.device.vkDevice(), writes, null);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VK12.vkCmdBindDescriptorSets(commandBuffer, bindPoint, layout, set, stack.longs(descriptorSet), null);
        }
    }
}
//?}
