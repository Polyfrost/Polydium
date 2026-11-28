package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.mojang.renderpearl.backend.vulkan.VulkanInstance;
import com.mojang.renderpearl.backend.vulkan.VulkanPhysicalDevice;
import com.mojang.renderpearl.backend.vulkan.checkpoints.CheckpointExtension;
import com.mojang.renderpearl.backend.vulkan.checkpoints.NoopCheckpointExtension;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import org.lwjgl.vulkan.VkDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Set;

@Mixin(VulkanBackend.class)
public abstract class MixinVulkanCheckpointsOff {
    @Unique
    private static final boolean polydium$CHECKPOINTS = Boolean.getBoolean("polydium.vulkan.checkpoints");

    @ModifyExpressionValue(
        method = "createDevice(Lcom/mojang/renderpearl/api/device/GpuDebugOptions;)Lcom/mojang/renderpearl/api/device/GpuDevice;",
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanFeatureSets;optionalFeatureSets()Ljava/util/Set;")
    )
    private Set<FeatureSet> polydium$dropCheckpoints(Set<FeatureSet> optional) {
        if (!polydium$CHECKPOINTS) {
            optional.remove(VulkanFeatureSets.AMD_BUFFER_MARKER_FEATURESET);
            optional.remove(VulkanFeatureSets.NV_DIAGNOSTIC_CHECKPOINT_FEATURESET);
        }
        return optional;
    }

    @ModifyArg(
        method = "createDevice(Lcom/mojang/renderpearl/api/device/GpuDebugOptions;)Lcom/mojang/renderpearl/api/device/GpuDevice;",
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanDevice;<init>(Lcom/mojang/renderpearl/backend/vulkan/VulkanInstance;Lcom/mojang/renderpearl/backend/vulkan/VulkanPhysicalDevice;Lcom/mojang/renderpearl/backend/vulkan/init/FeatureSet;Lorg/lwjgl/vulkan/VkDevice;JLcom/mojang/renderpearl/backend/vulkan/checkpoints/CheckpointExtension;)V"),
        index = 5
    )
    private CheckpointExtension polydium$requireSync2(
        VulkanInstance instance, VulkanPhysicalDevice physicalDevice, FeatureSet enabled, VkDevice device, long vma, CheckpointExtension checkpoints
    ) {
        if (checkpoints != NoopCheckpointExtension.INSTANCE && !enabled.extensions().contains("VK_KHR_synchronization2")) {
            LogUtils.getLogger().warn("Vulkan checkpoints need synchronization2, disabling them");
            return NoopCheckpointExtension.INSTANCE;
        }
        return checkpoints;
    }
}
//?} else if >= 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.vulkan.VulkanBackend;
import com.mojang.blaze3d.vulkan.VulkanInstance;
import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import com.mojang.blaze3d.vulkan.checkpoints.CheckpointExtension;
import com.mojang.blaze3d.vulkan.checkpoints.NoopCheckpointExtension;
import com.mojang.logging.LogUtils;
import org.lwjgl.vulkan.VkDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Set;

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

    @ModifyArg(
        method = "createDevice(JLcom/mojang/blaze3d/shaders/ShaderSource;Lcom/mojang/blaze3d/shaders/GpuDebugOptions;Ljava/lang/Runnable;)Lcom/mojang/blaze3d/systems/GpuDevice;",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanDevice;<init>(Lcom/mojang/blaze3d/shaders/ShaderSource;Lcom/mojang/blaze3d/vulkan/VulkanInstance;Lcom/mojang/blaze3d/vulkan/VulkanPhysicalDevice;Ljava/util/Set;Lorg/lwjgl/vulkan/VkDevice;JLcom/mojang/blaze3d/vulkan/checkpoints/CheckpointExtension;)V"),
        index = 6
    )
    private CheckpointExtension polydium$requireSync2(
        ShaderSource shaderSource, VulkanInstance instance, VulkanPhysicalDevice physicalDevice, Set<String> extensions, VkDevice device, long vma, CheckpointExtension checkpoints
    ) {
        if (checkpoints != NoopCheckpointExtension.INSTANCE && !extensions.contains("VK_KHR_synchronization2")) {
            LogUtils.getLogger().warn("Vulkan checkpoints need synchronization2, disabling them");
            return NoopCheckpointExtension.INSTANCE;
        }
        return checkpoints;
    }
}
*///?}
