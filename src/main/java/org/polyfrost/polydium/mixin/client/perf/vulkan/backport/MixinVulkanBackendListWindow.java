package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

//? if < 26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.BackendCreationException;
import com.mojang.logging.LogUtils;
import org.objectweb.asm.Opcodes;
import org.polyfrost.polydium.client.VulkanAvailability;
import org.polyfrost.polydium.vulkan.EssentialVulkan;
import org.polyfrost.polydium.vulkan.SodiumExtraMac;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.VulkanBackend;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinVulkanBackendListWindow {
    @Shadow private int width;
    @Shadow private int height;
    @Shadow private int framebufferWidth;
    @Shadow private int framebufferHeight;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J", remap = false))
    private void polydium$pickVulkan(CallbackInfo ci) {
        BackendCreationException unavailable = VulkanBackend.checkBackendAvailable();
        VulkanAvailability.record(unavailable == null ? null : unavailable.getMessage());
        if (unavailable != null) {
            LogUtils.getLogger().info("Vulkan backend unavailable, using OpenGL: {}", unavailable.getMessage());
            return;
        }

        EssentialVulkan.registerMixins();
        VulkanActive.backend = new VulkanBackend();
        VulkanActive.backend.setWindowHints();
    }

    @Inject(method = "refreshFramebufferSize", at = @At("RETURN"))
    private void polydium$macReduceRefresh(CallbackInfo ci) {
        this.polydium$macReduce();
    }

    @Inject(method = "onFramebufferResize", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/Window;framebufferHeight:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void polydium$macReduceResize(long handle, int newWidth, int newHeight, CallbackInfo ci) {
        this.polydium$macReduce();
    }

    @Unique
    private void polydium$macReduce() {
        if (VulkanActive.backend != null && SodiumExtraMac.isHighDpi(this.framebufferWidth, this.framebufferHeight, this.width, this.height) && SodiumExtraMac.enabled()) {
            this.framebufferWidth = Math.max(1, this.framebufferWidth / 2);
            this.framebufferHeight = Math.max(1, this.framebufferHeight / 2);
        }
    }

    @WrapOperation(method = "updateVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V", remap = false))
    private void polydium$vsync(int interval, Operation<Void> original) {
        if (VulkanDevice.active != null) {
            VulkanDevice.active.setVsync(interval != 0);
        } else {
            original.call(interval);
        }
    }
}
*///?}
