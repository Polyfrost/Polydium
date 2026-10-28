package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vulkan.VulkanBindGroupLayout;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.polyfrost.polydium.client.perf.VulkanLegacyRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanBindGroupLayout.class)
public abstract class MixinVulkanLegacyBindGroupLayout {
    @WrapOperation(method = "create", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkDescriptorSetLayoutCreateInfo;flags(I)Lorg/lwjgl/vulkan/VkDescriptorSetLayoutCreateInfo;"))
    private static VkDescriptorSetLayoutCreateInfo polydium$plainSetLayout(VkDescriptorSetLayoutCreateInfo createInfo, int flags, Operation<VkDescriptorSetLayoutCreateInfo> original, @Local(argsOnly = true) VulkanDevice device) {
        return original.call(createInfo, ((VulkanLegacyRendering.Device) device).polydium$pushDescriptors() ? flags : 0);
    }
}
*///?}
