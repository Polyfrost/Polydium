package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.objectweb.asm.Opcodes;
import org.polyfrost.polydium.client.perf.VulkanDescriptorWrites;
import org.polyfrost.polydium.vulkan.VulkanBindGroupLayout;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanConst;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.polyfrost.polydium.vulkan.VulkanRenderPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.List;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanDescriptorWriteBackport {
    @Shadow
    @Final
    private VulkanDevice device;

    @Shadow
    @Final
    private VulkanCommandEncoder encoder;

    @Shadow
    @Final
    private VkCommandBuffer commandBuffer;

    @Shadow
    protected VulkanRenderPipeline pipeline;

    @Shadow
    private boolean anyDescriptorDirty;

    @Shadow
    @Final
    protected HashMap<String, GpuBufferSlice> uniforms;

    @Shadow
    @Final
    protected HashMap<String, VulkanRenderPass.TextureViewAndSampler> textures;

    @ModifyExpressionValue(method = "pushDescriptors", at = @At(value = "FIELD", target = "Lorg/polyfrost/polydium/vulkan/VulkanRenderPass;anyDescriptorDirty:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    private boolean polydium$writeDescriptors(boolean dirty) {
        return dirty && !(this.pipeline != null && !VulkanRenderPass.VALIDATION && this.polydium$write());
    }

    @Unique
    private boolean polydium$write() {
        VulkanRenderPipeline pipeline = this.pipeline;
        List<VulkanBindGroupLayout.Entry> entries = pipeline.layout().entries();
        int count = entries.size();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long writes = VulkanDescriptorWrites.writes(stack, count);
            for (int index = 0; index < count; index++) {
                VulkanBindGroupLayout.Entry entry = entries.get(index);
                VulkanBindGroupLayout.VulkanBindGroupEntryType type = entry.type();
                if (type == VulkanBindGroupLayout.VulkanBindGroupEntryType.SAMPLED_IMAGE) {
                    VulkanRenderPass.TextureViewAndSampler texture = this.textures.get(entry.name());
                    if (texture == null) {
                        return false;
                    }
                    VulkanDescriptorWrites.imageSampler(stack, VulkanDescriptorWrites.write(writes, index), texture.sampler().vkSampler(), texture.view().vkImageView());
                    continue;
                }
                GpuBufferSlice value = this.uniforms.get(entry.name());
                if (value == null) {
                    return false;
                }
                if (type == VulkanBindGroupLayout.VulkanBindGroupEntryType.UNIFORM_BUFFER) {
                    VulkanDescriptorWrites.uniformBuffer(stack, VulkanDescriptorWrites.write(writes, index), value);
                } else if (type == VulkanBindGroupLayout.VulkanBindGroupEntryType.TEXEL_BUFFER) {
                    VulkanDescriptorWrites.texelBuffer(stack, VulkanDescriptorWrites.write(writes, index), value, VulkanConst.toVk(entry.texelBufferFormat()), this.device, this.encoder);
                } else {
                    VulkanDescriptorWrites.write(writes, index);
                }
            }
            VulkanDescriptorWrites.push(this.commandBuffer, pipeline.pipelineLayout(), count, writes);
        }
        this.anyDescriptorDirty = false;
        return true;
    }
}
