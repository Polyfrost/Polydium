package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.3 {
import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(VulkanFeatureSets.class)
public abstract class MixinVulkanCheckpointsOff {
    @Inject(method = "optionalFeatureSets", at = @At("RETURN"))
    private static void polydium$dropCheckpoints(CallbackInfoReturnable<Set<FeatureSet>> cir) {
        if (Boolean.getBoolean("polydium.vulkan.checkpoints")) {
            return;
        }
        cir.getReturnValue().remove(VulkanFeatureSets.AMD_BUFFER_MARKER_FEATURESET);
        cir.getReturnValue().remove(VulkanFeatureSets.NV_DIAGNOSTIC_CHECKPOINT_FEATURESET);
    }
}
//?} else if >= 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vulkan.VulkanBackend;
import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanBackend.class)
public abstract class MixinVulkanCheckpointsOff {
    @Unique
    private static final boolean polydium$CHECKPOINTS = Boolean.getBoolean("polydium.vulkan.checkpoints");

    @WrapOperation(
        method = "createDevice(JLcom/mojang/blaze3d/shaders/ShaderSource;Lcom/mojang/blaze3d/shaders/GpuDebugOptions;Ljava/lang/Runnable;)Lcom/mojang/blaze3d/systems/GpuDevice;",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanPhysicalDevice;hasDeviceExtension(Ljava/lang/String;)Z")
    )
    private boolean polydium$hideCheckpointExtensions(VulkanPhysicalDevice physicalDevice, String extension, Operation<Boolean> original) {
        if (!polydium$CHECKPOINTS && (extension.equals("VK_AMD_buffer_marker") || extension.equals("VK_NV_device_diagnostic_checkpoints"))) {
            return false;
        }
        return original.call(physicalDevice, extension);
    }
}
*///?}
