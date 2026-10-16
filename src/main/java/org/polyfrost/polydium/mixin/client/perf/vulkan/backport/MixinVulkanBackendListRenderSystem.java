package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

//? if < 26.1 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.GpuDebugOptions;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.SamplerCache;
import net.minecraft.client.renderer.DynamicUniforms;
import org.jspecify.annotations.Nullable;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderSystem.class)
public class MixinVulkanBackendListRenderSystem {
    @Shadow private static @Nullable GpuDevice DEVICE;
    @Shadow private static String apiDescription;
    @Shadow private static @Nullable DynamicUniforms dynamicUniforms;
    @Shadow private static SamplerCache samplerCache;

    @WrapMethod(method = "initRenderer")
    private static void polydium$vulkanDevice(long window, int debugVerbosity, boolean synchronousLogs, ShaderSource defaultShaderSource,
                                              boolean renderDebugLabels, Operation<Void> original) {
        if (VulkanActive.backend == null) {
            original.call(window, debugVerbosity, synchronousLogs, defaultShaderSource, renderDebugLabels);
            return;
        }

        DEVICE = VulkanActive.backend.createDevice(window, defaultShaderSource, new GpuDebugOptions(debugVerbosity, synchronousLogs, renderDebugLabels));
        apiDescription = DEVICE.getImplementationInformation();
        dynamicUniforms = new DynamicUniforms();
        samplerCache.initialize();
    }

    @WrapOperation(method = "flipFrame", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V", remap = false))
    private static void polydium$present(long window, Operation<Void> original) {
        if (VulkanDevice.active != null) {
            VulkanDevice.active.presentFrame();
        } else {
            original.call(window);
        }
    }
}
*///?}
