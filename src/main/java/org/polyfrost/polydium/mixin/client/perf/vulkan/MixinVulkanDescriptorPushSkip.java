package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
//? if < 26.3 {
/*import com.mojang.blaze3d.vulkan.VulkanBindGroupLayout;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.HashMap;
import java.util.List;
*///?}
import it.unimi.dsi.fastutil.objects.ReferenceList;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanDescriptorPushSkip {
    @Shadow
    protected VulkanRenderPipeline pipeline;

    @Shadow
    private boolean anyDescriptorDirty;

    @Shadow
    @Final
    private VkCommandBuffer commandBuffer;

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

    @Unique
    private static final ReferenceArrayList<Object> POLYDIUM$VALUES = new ReferenceArrayList<>();
    *///?}

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
    private boolean polydium$pushChangedDescriptors(VulkanRenderPass pass) {
        if (!this.anyDescriptorDirty) {
            return false;
        }
        VulkanRenderPipeline pipeline = this.pipeline;
        if (pipeline != null) {
            if (VulkanDynamicState.changesDescriptors(this.commandBuffer.address(), pipeline.pipelineLayout(), this.polydium$values(pipeline))) {
                return true;
            }
            this.anyDescriptorDirty = false;
            return false;
        }
        VulkanDynamicState.forgetDescriptors();
        return true;
    }

    @Unique
    private ReferenceList<Object> polydium$values(VulkanRenderPipeline pipeline) {
        //? if >= 26.3 {
        return this.uniforms;
        //?} else {
        /*ReferenceArrayList<Object> values = POLYDIUM$VALUES;
        List<VulkanBindGroupLayout.Entry> entries = pipeline.layout().entries();
        values.clear();
        for (int index = 0; index < entries.size(); index++) {
            VulkanBindGroupLayout.Entry entry = entries.get(index);
            values.add(entry.type() == VulkanBindGroupLayout.VulkanBindGroupEntryType.SAMPLED_IMAGE ? this.textures.get(entry.name()) : this.uniforms.get(entry.name()));
        }
        return values;
        *///?}
    }
}
//?}
