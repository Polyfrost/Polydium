package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.function.Consumer;

@Mixin(VulkanDevice.class)
public abstract class MixinVulkanDeferPipelineDestroy {
    @Shadow
    @Final
    private VulkanCommandEncoder commandEncoder;

    @Shadow
    public abstract void clearPipelineCache();

    // the command buffer being recorded may still have these bound
    @WrapOperation(
        method = "clearPipelineCache",
        at = @At(value = "INVOKE", target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V", ordinal = 0)
    )
    private void polydium$deferPipelineDestroy(Collection<VulkanRenderPipeline> pipelines, Consumer<VulkanRenderPipeline> destroy, Operation<Void> original) {
        original.call(pipelines, (Consumer<VulkanRenderPipeline>) this.commandEncoder::queueForDestroy);
    }

    // vanilla clears after the encoder's destroy, which would leave the queued pipelines undestroyed
    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;destroy()V"))
    private void polydium$clearPipelinesBeforeEncoderDestroy(CallbackInfo ci) {
        this.clearPipelineCache();
    }
}
*///?}
