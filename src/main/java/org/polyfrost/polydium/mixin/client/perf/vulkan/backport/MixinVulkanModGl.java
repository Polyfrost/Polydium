package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_enableBlend()V"), require = 0)
    private static void polydium$enableBlend() {
        if (!VulkanActive.active) GlStateManager._enableBlend();
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_disableBlend()V"), require = 0)
    private static void polydium$disableBlend() {
        if (!VulkanActive.active) GlStateManager._disableBlend();
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_enableDepthTest()V"), require = 0)
    private static void polydium$enableDepthTest() {
        if (!VulkanActive.active) GlStateManager._enableDepthTest();
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_disableDepthTest()V"), require = 0)
    private static void polydium$disableDepthTest() {
        if (!VulkanActive.active) GlStateManager._disableDepthTest();
    }

    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetString(I)Ljava/lang/String;"), require = 0)
    private static String polydium$glGetString(int name) {
        return VulkanActive.active ? RenderSystem.getDevice().getVendor() : GL11.glGetString(name);
    }

    @Redirect(method = "takeScreenshot", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glReadPixels(IIIIIILjava/nio/FloatBuffer;)V"), require = 0)
    private static void polydium$glReadPixels(int x, int y, int width, int height, int format, int type, FloatBuffer pixels) {
        if (!VulkanActive.active) GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetIntegerv(I[I)V"), require = 0)
    private static void polydium$glGetIntegerv(int pname, int[] params) {
        if (!VulkanActive.active) GL11.glGetIntegerv(pname, params);
    }

    @Redirect(method = {"paint", "setGlProperty"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"), require = 0)
    private static void polydium$glEnable(int cap) {
        if (!VulkanActive.active) GL11.glEnable(cap);
    }

    @Redirect(method = {"paint", "setGlProperty"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"), require = 0)
    private static void polydium$glDisable(int cap) {
        if (!VulkanActive.active) GL11.glDisable(cap);
    }

    @Redirect(method = "refreshScissors", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glScissor(IIII)V"), require = 0)
    private static void polydium$glScissor(int x, int y, int width, int height) {
        if (!VulkanActive.active) GL11.glScissor(x, y, width, height);
    }

    @Redirect(method = {"begin", "end"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11C;glFinish()V"), require = 0)
    private static void polydium$glFinish() {
        if (!VulkanActive.active) GL11C.glFinish();
    }

    @Inject(method = {"resetState", "enableDepthTest", "disableDepthTest", "depthMask", "enableBlend", "blendFunc"}, at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$skipGlReset(CallbackInfo ci) {
        if (VulkanActive.active) ci.cancel();
    }

    @Inject(method = "initVoxyClient", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$skipVoxy(CallbackInfo ci) {
        if (!VulkanActive.active) return;
        org.slf4j.LoggerFactory.getLogger("polydium/vulkan").warn("Voxy only renders through OpenGL; disabled while the Vulkan backend is active");
        ci.cancel();
    }
}
