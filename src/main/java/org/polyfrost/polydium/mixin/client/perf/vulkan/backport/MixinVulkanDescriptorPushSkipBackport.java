package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.polyfrost.polydium.vulkan.VulkanBindGroupLayout;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.polyfrost.polydium.vulkan.VulkanRenderPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanDescriptorPushSkipBackport {
    @Shadow
    protected VulkanRenderPipeline pipeline;

    @Shadow
    private boolean anyDescriptorDirty;

    @Shadow
    @Final
    private VkCommandBuffer commandBuffer;

    @Shadow
    private Object[] uniformSlots;

    @Shadow
    private Object[] textureSlots;

    @WrapWithCondition(
        method = {
            "drawIndexed(IIIII)V",
            "multiDrawIndexed(Ljava/nio/IntBuffer;III)V",
            "drawIndexedIndirect(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;I)V",
            "draw(IIII)V",
            "multiDraw(Ljava/nio/IntBuffer;III)V",
            "drawIndirect(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;I)V"
        },
        at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanRenderPass;pushDescriptors()V")
    )
    private boolean polydium$pushChangedDescriptors(VulkanRenderPass pass) {
        if (!this.anyDescriptorDirty) {
            return false;
        }

        VulkanRenderPipeline pipeline = this.pipeline;
        if (pipeline != null) {
            VulkanBindGroupLayout layout = pipeline.layout();
            if (VulkanDynamicState.changesDescriptors(this.commandBuffer.address(), pipeline.pipelineLayout(),
                layout.entries(), layout.slots(), this.textureSlots, this.uniformSlots)) {
                return true;
            }
            this.anyDescriptorDirty = false;
            return false;
        }

        VulkanDynamicState.forgetDescriptors();
        return true;
    }
}
