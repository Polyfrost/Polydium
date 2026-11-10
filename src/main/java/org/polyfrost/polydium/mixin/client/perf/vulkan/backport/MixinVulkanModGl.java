package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
    "com.misterpemodder.shulkerboxtooltip.impl.config.gui.ConfigScreen",
    "dev.tr7zw.trender.gui.client.CottonClientScreen",
    "dev.tr7zw.trender.gui.client.Scissors",
    "org.polyfrost.polyblur.client.blur.BlurProfiler",
    "fudge.notenoughcrashes.utils.GlUtil",
    "me.fallenbreath.tweakermore.util.render.context.RenderGlobals",
    "wily.factoryapi.util.FactoryScreenUtil",
    "me.fallenbreath.pistorder.impl.StringDrawer",
    "com.teamresourceful.resourcefullib.client.sysinfo.defaults.OperatingSystem",
    "com.tom.cpm.client.Panel3dImpl",
    "io.wispforest.owo.ui.base.BaseOwoContainerScreen$SlotComponent",
    "me.cortex.voxy.client.VoxyClient",
    "main.walksy.lib.core.renderer.Renderer2D"
}, remap = false)
public class MixinVulkanModGl {

    @WrapWithCondition(method = "*", at = {
        @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_enableBlend()V"),
        @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_disableBlend()V"),
        @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_enableDepthTest()V"),
        @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_disableDepthTest()V")
    }, require = 0)
    private static boolean polydium$glState() {
        return !VulkanActive.active;
    }

    @WrapOperation(method = "accept", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetString(I)Ljava/lang/String;"), require = 0)
    private static String polydium$glGetString(int name, Operation<String> original) {
        return VulkanActive.active ? RenderSystem.getDevice().getVendor() : original.call(name);
    }

    @WrapWithCondition(method = "takeScreenshot", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glReadPixels(IIIIIILjava/nio/FloatBuffer;)V"), require = 0)
    private static boolean polydium$glReadPixels(int x, int y, int width, int height, int format, int type, FloatBuffer pixels) {
        return !VulkanActive.active;
    }

    @WrapWithCondition(method = "draw", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetIntegerv(I[I)V"), require = 0)
    private static boolean polydium$glGetIntegerv(int pname, int[] params) {
        return !VulkanActive.active;
    }

    @WrapWithCondition(method = {"paint", "setGlProperty"}, at = {
        @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"),
        @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V")
    }, require = 0)
    private static boolean polydium$glToggle(int cap) {
        return !VulkanActive.active;
    }

    @WrapWithCondition(method = "refreshScissors", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glScissor(IIII)V"), require = 0)
    private static boolean polydium$glScissor(int x, int y, int width, int height) {
        return !VulkanActive.active;
    }

    @WrapWithCondition(method = {"begin", "end"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11C;glFinish()V"), require = 0)
    private static boolean polydium$glFinish() {
        return !VulkanActive.active;
    }

    @Inject(method = {"resetState", "enableDepthTest", "disableDepthTest", "depthMask", "enableBlend", "blendFunc"}, at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$skipGlReset(CallbackInfo ci) {
        if (VulkanActive.active) ci.cancel();
    }

    @Inject(method = "initVoxyClient", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$skipVoxy(CallbackInfo ci) {
        if (!VulkanActive.active) return;
        LoggerFactory.getLogger("polydium/vulkan").warn("Voxy only renders through OpenGL; disabled while the Vulkan backend is active");
        ci.cancel();
    }
}
