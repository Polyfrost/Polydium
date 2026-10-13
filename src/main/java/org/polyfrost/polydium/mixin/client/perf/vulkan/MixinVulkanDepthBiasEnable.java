package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VulkanRenderPipeline.class)
public abstract class MixinVulkanDepthBiasEnable {

    @ModifyArg(method = "compile", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkPipelineRasterizationStateCreateInfo;depthBiasEnable(Z)Lorg/lwjgl/vulkan/VkPipelineRasterizationStateCreateInfo;"))
    private static boolean polydium$enableForEitherFactor(boolean enable, @Local(argsOnly = true) RenderPipeline pipeline) {
        return pipeline.getDepthStencilState().depthBiasConstant() != 0.0F || pipeline.getDepthStencilState().depthBiasScaleFactor() != 0.0F;
    }
}
*///?}
