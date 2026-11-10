package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.opengl.GlStateManager;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GlStateManager.class)
public class MixinVulkanBackendListGlState {

    @WrapMethod(method = {
        "_enableBlend", "_disableBlend", "_enableDepthTest", "_disableDepthTest",
        "_enableCull", "_disableCull", "_enableScissorTest", "_disableScissorTest"
    })
    private static void polydium$skipOnVulkan(Operation<Void> original) {
        if (!VulkanActive.active) original.call();
    }

    @WrapMethod(method = "_depthMask")
    private static void polydium$skipOnVulkan(boolean mask, Operation<Void> original) {
        if (!VulkanActive.active) original.call(mask);
    }

    @WrapMethod(method = {"_blendFuncSeparate", "_scissorBox"})
    private static void polydium$skipOnVulkan(int a, int b, int c, int d, Operation<Void> original) {
        if (!VulkanActive.active) original.call(a, b, c, d);
    }

    //? if >= 26.1 {
    @WrapMethod(method = "_colorMask")
    private static void polydium$skipOnVulkan(int writeMask, Operation<Void> original) {
        if (!VulkanActive.active) original.call(writeMask);
    }
    //?} else {
    /*@WrapMethod(method = "_colorMask")
    private static void polydium$skipOnVulkan(boolean red, boolean green, boolean blue, boolean alpha, Operation<Void> original) {
        if (!VulkanActive.active) original.call(red, green, blue, alpha);
    }
    *///?}
}
