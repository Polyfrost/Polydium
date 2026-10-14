package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

//? if < 26.1 {
/*import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.BackendCreationException;
import com.mojang.logging.LogUtils;
import org.lwjgl.glfw.GLFW;
import org.polyfrost.polydium.vulkan.EssentialVulkan;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.VulkanBackend;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinVulkanBackendListWindow {

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J", remap = false))
    private void polydium$pickVulkan(CallbackInfo ci) {
        BackendCreationException unavailable = VulkanBackend.checkBackendAvailable();
        if (unavailable != null) {
            LogUtils.getLogger().info("Vulkan backend unavailable, using OpenGL: {}", unavailable.getMessage());
            return;
        }

        EssentialVulkan.registerMixins();
        VulkanActive.backend = new VulkanBackend();
        VulkanActive.backend.setWindowHints();
    }

    @Redirect(method = "updateVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V", remap = false))
    private void polydium$vsync(int interval) {
        if (VulkanDevice.active != null) {
            VulkanDevice.active.setVsync(interval != 0);
        } else {
            GLFW.glfwSwapInterval(interval);
        }
    }
}
*///?}
