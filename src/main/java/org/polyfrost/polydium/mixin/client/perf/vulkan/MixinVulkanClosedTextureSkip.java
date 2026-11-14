package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
//? if >= 26.3 {
import com.mojang.renderpearl.util.TextureViewAndSampler;
import it.unimi.dsi.fastutil.objects.ReferenceList;
//?} else {
/*import com.mojang.blaze3d.vulkan.VulkanBindGroupLayout;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
import java.util.HashMap;
import java.util.List;
*///?}
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDescriptorWrites;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

// vanilla only rejects closed texture views under IDE validation, and their VkImageView is freed a few frames after close
@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanClosedTextureSkip {
    @Shadow
    private boolean anyDescriptorDirty;

    //? if >= 26.3 {
    @Shadow
    @Final
    protected ReferenceList<Object> uniforms;
    //?} else {
    /*@Shadow
    protected VulkanRenderPipeline pipeline;

    @Shadow
    @Final
    protected HashMap<String, Object> textures;
    *///?}

    @Unique
    private boolean polydium$skipDraw;

    @WrapWithCondition(
        method = {
            "drawIndexed(IIIII)V",
            "multiDrawIndexed(Ljava/nio/IntBuffer;III)V",
            "drawIndexedIndirect(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;I)V",
            "draw(IIII)V",
            "multiDraw(Ljava/nio/IntBuffer;III)V",
            "drawIndirect(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;I)V"
        },
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanRenderPass;pushDescriptors()V")
    )
    private boolean polydium$refuseClosedTextures(VulkanRenderPass pass) {
        this.polydium$skipDraw = this.anyDescriptorDirty && this.polydium$samplesClosedTexture();
        if (this.polydium$skipDraw) {
            // the descriptor push skip may already have cached this set as bound
            VulkanDynamicState.forgetDescriptors();
        }
        return !this.polydium$skipDraw;
    }

    @Unique
    private boolean polydium$samplesClosedTexture() {
        //? if >= 26.3 {
        for (int index = 0; index < this.uniforms.size(); index++) {
            if (this.uniforms.get(index) instanceof TextureViewAndSampler texture && VulkanDescriptorWrites.closed(texture.view())) {
                return true;
            }
        }
        //?} else {
        /*List<VulkanBindGroupLayout.Entry> entries = this.pipeline.layout().entries();
        for (int index = 0; index < entries.size(); index++) {
            VulkanBindGroupLayout.Entry entry = entries.get(index);
            if (entry.type() == VulkanBindGroupLayout.VulkanBindGroupEntryType.SAMPLED_IMAGE
                && this.textures.get(entry.name()) instanceof VulkanTextureBindingAccessor texture && VulkanDescriptorWrites.closed(texture.polydium$view())) {
                return true;
            }
        }
        *///?}
        return false;
    }

    @WrapWithCondition(method = "drawIndexed(IIIII)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdDrawIndexed(Lorg/lwjgl/vulkan/VkCommandBuffer;IIIII)V"))
    private boolean polydium$drawIndexed(VkCommandBuffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance) {
        return !this.polydium$skipDraw;
    }

    @WrapWithCondition(method = "multiDrawIndexed(Ljava/nio/IntBuffer;III)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/EXTMultiDraw;nvkCmdDrawMultiIndexedEXT(Lorg/lwjgl/vulkan/VkCommandBuffer;IJIIIJ)V"))
    private boolean polydium$multiDrawIndexed(VkCommandBuffer commandBuffer, int drawCount, long indexInfo, int instanceCount, int firstInstance, int stride, long vertexOffset) {
        return !this.polydium$skipDraw;
    }

    @WrapWithCondition(method = "draw(IIII)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdDraw(Lorg/lwjgl/vulkan/VkCommandBuffer;IIII)V"))
    private boolean polydium$draw(VkCommandBuffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
        return !this.polydium$skipDraw;
    }

    @WrapWithCondition(method = "multiDraw(Ljava/nio/IntBuffer;III)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/EXTMultiDraw;nvkCmdDrawMultiEXT(Lorg/lwjgl/vulkan/VkCommandBuffer;IJIII)V"))
    private boolean polydium$multiDraw(VkCommandBuffer commandBuffer, int drawCount, long vertexInfo, int instanceCount, int firstInstance, int stride) {
        return !this.polydium$skipDraw;
    }

    @WrapWithCondition(
        method = {
            "drawIndexedIndirect(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;I)V",
            "drawIndirect(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;I)V"
        },
        at = {
            @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdDrawIndexedIndirect(Lorg/lwjgl/vulkan/VkCommandBuffer;JJII)V"),
            @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdDrawIndirect(Lorg/lwjgl/vulkan/VkCommandBuffer;JJII)V")
        }
    )
    private boolean polydium$drawIndirect(VkCommandBuffer commandBuffer, long buffer, long offset, int drawCount, int stride) {
        return !this.polydium$skipDraw;
    }
}
//?}
