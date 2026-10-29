package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
//? if >= 26.3 {
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSampler;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import com.mojang.renderpearl.util.TextureViewAndSampler;
import it.unimi.dsi.fastutil.objects.ReferenceList;
//?} else {
/*import com.mojang.blaze3d.vulkan.VulkanBindGroupLayout;
import com.mojang.blaze3d.vulkan.VulkanConst;
import java.util.HashMap;
*///?}
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.objectweb.asm.Opcodes;
import org.polyfrost.polydium.client.perf.VulkanDescriptorWrites;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanDescriptorWrite {
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

    //? if >= 26.3 {
    @Shadow
    @Final
    protected ReferenceList<Object> uniforms;
    //?} else {
    /*@Shadow
    @Final
    protected HashMap<String, GpuBufferSlice> uniforms;

    @Shadow
    @Final
    protected HashMap<String, Object> textures;
    *///?}

    @ModifyExpressionValue(method = "pushDescriptors", at = @At(value = "FIELD", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanRenderPass;anyDescriptorDirty:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    private boolean polydium$writeDescriptors(boolean dirty) {
        return dirty && !(this.pipeline != null && this.polydium$write());
    }

    @Unique
    private boolean polydium$write() {
        VulkanRenderPipeline pipeline = this.pipeline;
        //? if >= 26.3 {
        List<BindGroupLayout.UniformDescription> entries = pipeline.uniforms();
        //?} else {
        /*if (VulkanRenderPass.VALIDATION) {
            return false;
        }
        List<VulkanBindGroupLayout.Entry> entries = pipeline.layout().entries();
        *///?}
        int count = entries.size();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long writes = VulkanDescriptorWrites.writes(stack, count);
            for (int index = 0; index < count; index++) {
                //? if >= 26.3 {
                BindGroupLayout.UniformDescription entry = entries.get(index);
                UniformType type = entry.type();
                Object value = this.uniforms.get(index);
                if (value == null) {
                    return false;
                }
                long write = VulkanDescriptorWrites.write(writes, index);
                if (type == UniformType.UNIFORM_BUFFER) {
                    VulkanDescriptorWrites.uniformBuffer(stack, write, (GpuBufferSlice) value);
                } else if (type == UniformType.COMBINED_IMAGE_SAMPLER) {
                    TextureViewAndSampler texture = (TextureViewAndSampler) value;
                    VulkanDescriptorWrites.imageSampler(stack, write, ((VulkanGpuSampler) texture.sampler()).vkSampler(), ((VulkanGpuTextureView) texture.view()).vkImageView());
                } else if (type == UniformType.TEXEL_BUFFER) {
                    VulkanDescriptorWrites.texelBuffer(stack, write, (GpuBufferSlice) value, VulkanConst.toVk(entry.gpuFormat()), this.device, this.encoder);
                }
                //?} else {
                /*VulkanBindGroupLayout.Entry entry = entries.get(index);
                VulkanBindGroupLayout.VulkanBindGroupEntryType type = entry.type();
                Object value = type == VulkanBindGroupLayout.VulkanBindGroupEntryType.SAMPLED_IMAGE ? this.textures.get(entry.name()) : this.uniforms.get(entry.name());
                if (value == null) {
                    return false;
                }
                long write = VulkanDescriptorWrites.write(writes, index);
                if (type == VulkanBindGroupLayout.VulkanBindGroupEntryType.UNIFORM_BUFFER) {
                    VulkanDescriptorWrites.uniformBuffer(stack, write, (GpuBufferSlice) value);
                } else if (type == VulkanBindGroupLayout.VulkanBindGroupEntryType.SAMPLED_IMAGE) {
                    VulkanTextureBindingAccessor texture = (VulkanTextureBindingAccessor) value;
                    VulkanDescriptorWrites.imageSampler(stack, write, texture.polydium$sampler().vkSampler(), texture.polydium$view().vkImageView());
                } else if (type == VulkanBindGroupLayout.VulkanBindGroupEntryType.TEXEL_BUFFER) {
                    VulkanDescriptorWrites.texelBuffer(stack, write, (GpuBufferSlice) value, VulkanConst.toVk(entry.texelBufferFormat()), this.device, this.encoder);
                }
                *///?}
            }
            VulkanDescriptorWrites.push(this.commandBuffer, pipeline.pipelineLayout(), count, writes);
        }
        this.anyDescriptorDirty = false;
        return true;
    }
}
//?}
