package org.polyfrost.polydium.mixin.client.perf;

//? if < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.perf.GpuWaitMeter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = /^? if >= 26.1 {^/"com.mojang.blaze3d.opengl.GlDevice"/^?} else {^//^"com.mojang.blaze3d.systems.RenderSystem"^//^?}^/)
public abstract class MixinGlSwapWaitMeter {
    @WrapOperation(method = /^? if >= 26.1 {^/"presentFrame"/^?} else {^//^"flipFrame"^//^?}^/, at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V", remap = false))
    private /^? if < 26.1 {^//^static ^//^?}^/void polydium$timeSwap(long window, Operation<Void> original) {
        if (Minecraft.getInstance().options.enableVsync().get()) {
            original.call(window);
            return;
        }
        GpuWaitMeter.beginWait();
        original.call(window);
        GpuWaitMeter.endWait();
    }
}
*///?}
