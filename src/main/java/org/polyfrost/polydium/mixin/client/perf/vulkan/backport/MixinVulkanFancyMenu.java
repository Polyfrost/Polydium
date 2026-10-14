package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
    "de.keksuccino.fancymenu.util.rendering.glsl.GlslShaderRuntime",
    "de.keksuccino.fancymenu.util.rendering.GuiBlurRenderer"
}, remap = false)
public class MixinVulkanFancyMenu {

    @Inject(
        method = {
            "render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIFLjava/lang/String;Lde/keksuccino/fancymenu/util/rendering/glsl/GlslShaderRuntime$RenderSettings;)Z",
            "render(Lnet/minecraft/class_332;IIIIFLjava/lang/String;Lde/keksuccino/fancymenu/util/rendering/glsl/GlslShaderRuntime$RenderSettings;)Z"
        },
        at = @At("HEAD"), cancellable = true, require = 0
    )
    private void polydium$noGlslOnVulkan(CallbackInfoReturnable<Boolean> cir) {
        if (VulkanActive.active) cir.setReturnValue(false);
    }

    @Redirect(method = {"executeQueuedBlurArea_FancyMenu", "_renderBlurArea"}, at = @At(value = "INVOKE", target = "Ljava/lang/ThreadLocal;set(Ljava/lang/Object;)V"), require = 0)
    private static void polydium$setScissor(ThreadLocal<Object> local, Object scissor) {
        local.set(scissor);
        if (VulkanActive.active && scissor instanceof VulkanActive.Scissor s) VulkanActive.postPassScissor = s;
    }

    @Redirect(method = {"executeQueuedBlurArea_FancyMenu", "_renderBlurArea"}, at = @At(value = "INVOKE", target = "Ljava/lang/ThreadLocal;remove()V"), require = 0)
    private static void polydium$clearScissor(ThreadLocal<Object> local) {
        local.remove();
        VulkanActive.postPassScissor = null;
    }
}
