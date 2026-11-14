package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
//?} else {
/*import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.At;
*///?}
//? if >= 1.21.11 && < 26.1
/*import org.polyfrost.polydium.vulkan.VulkanActive;*/

@Mixin(targets = /*? if >= 26.2 {*/"com.mojang.renderpearl.backend.opengl.GlSurface"/*?} elif >= 26.1 {*//*"com.mojang.renderpearl.backend.opengl.GlDevice"*//*?} else {*//*"com.mojang.blaze3d.systems.RenderSystem"*//*?}*/)
public abstract class MixinGpuProfileGlSwap {
    //? if >= 26.2 {
    @WrapMethod(method = "present")
    private void polydium$timeSwap(Operation<Void> original) {
        long start = GpuProfiler.presentStart();

        original.call();
        GpuProfiler.presentWait(start, true);
    }
    //?} else {
    /*@WrapOperation(method = /^? if >= 26.1 {^/"presentFrame"/^?} else {^//^"flipFrame"^//^?}^/, at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V", remap = false))
    private /^? if < 26.1 {^//^static ^//^?}^/void polydium$timeSwap(long window, Operation<Void> original) {
        //? if >= 1.21.11 && < 26.1 {
        /^if (VulkanActive.active) {
            original.call(window);
            return;
        }
        ^///?}
        long start = GpuProfiler.presentStart();

        original.call(window);
        GpuProfiler.presentWait(start, true);
    }
    *///?}
}
