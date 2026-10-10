package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.PushConstantCall;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.ByteBuffer;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanPushConstantCall {
    @WrapWithCondition(
        method = "pushConstants",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdPushConstants(Lorg/lwjgl/vulkan/VkCommandBuffer;JIILjava/nio/ByteBuffer;)V")
    )
    private boolean polydium$pushThroughDowncall(VkCommandBuffer commandBuffer, long layout, int stageFlags, int offset, ByteBuffer values) {
        return !(PushConstantCall.push(commandBuffer, layout, stageFlags, offset, values));
    }
}
//?}
