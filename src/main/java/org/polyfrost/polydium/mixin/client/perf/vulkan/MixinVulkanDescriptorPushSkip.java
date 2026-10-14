package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
//? if < 26.3 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vulkan.VulkanBindGroupLayout;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import java.util.HashMap;
*///?}
import it.unimi.dsi.fastutil.objects.ReferenceList;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.polyfrost.polydium.client.perf.VulkanDynamicState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
//? if < 26.3
/*import org.spongepowered.asm.mixin.Unique;*/
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
    private Object[] polydium$uniformSlots = VulkanDynamicState.NO_SLOTS;

    @Unique
    private Object[] polydium$textureSlots = VulkanDynamicState.NO_SLOTS;

    @WrapOperation(
        method = {
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;)V",
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;)V",
            "bindTexture"
        },
        at = @At(value = "INVOKE", target = "Ljava/util/HashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object polydium$mirrorSlot(HashMap<Object, Object> map, Object name, Object value, Operation<Object> original) {
        Object previous = original.call(map, name, value);
        if (!value.equals(previous)) {
            if (map == (Object) this.textures) {
                this.polydium$textureSlots = VulkanDynamicState.setSlot(this.polydium$textureSlots, (String) name, value);
            } else {
                this.polydium$uniformSlots = VulkanDynamicState.setSlot(this.polydium$uniformSlots, (String) name, value);
            }
        }
        return previous;
    }

    @WrapOperation(method = "bindTexture", at = @At(value = "INVOKE", target = "Ljava/util/HashMap;remove(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object polydium$clearSlot(HashMap<Object, Object> map, Object name, Operation<Object> original) {
        Object previous = original.call(map, name);
        if (previous != null) {
            this.polydium$textureSlots = VulkanDynamicState.setSlot(this.polydium$textureSlots, (String) name, null);
        }
        return previous;
    }

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
            //? if >= 26.3 {
            boolean changed = VulkanDynamicState.changesDescriptors(this.commandBuffer.address(), pipeline.pipelineLayout(), this.uniforms);
            //?} else {
            /*VulkanBindGroupLayout layout = pipeline.layout();
            boolean changed = VulkanDynamicState.changesDescriptors(this.commandBuffer.address(), pipeline.pipelineLayout(), layout.entries(),
                ((VulkanDynamicState.LayoutSlots) (Object) layout).polydium$slots(), this.polydium$textureSlots, this.polydium$uniformSlots);
            *///?}
            if (changed) {
                return true;
            }
            this.anyDescriptorDirty = false;
            return false;
        }
        VulkanDynamicState.forgetDescriptors();
        return true;
    }
}
//?}
