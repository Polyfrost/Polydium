package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.3 {
import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import com.mojang.renderpearl.backend.vulkan.init.VulkanFeature;
import com.mojang.renderpearl.backend.vulkan.init.VulkanPNextStruct;
import org.polyfrost.polydium.client.perf.VulkanLegacyRendering;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

// 26.4-snapshot-1 no longer requires VK_KHR_dynamic_rendering or VK_KHR_push_descriptor; they're enabled only when present
@Mixin(VulkanFeatureSets.class)
public abstract class MixinVulkanLegacyFeatures {
    @Shadow
    @Final
    @Mutable
    public static FeatureSet REQUIRED_FEATURESET;

    @Shadow
    @Final
    public static VulkanPNextStruct DYNAMIC_RENDERING_FEATURES_STRUCT;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void polydium$relaxRequired(CallbackInfo ci) {
        FeatureSet required = REQUIRED_FEATURESET;
        Set<String> extensions = new HashSet<>(required.extensions());
        extensions.remove("VK_KHR_dynamic_rendering");
        extensions.remove("VK_KHR_push_descriptor");
        Set<VulkanFeature> features = new HashSet<>(required.features());
        features.removeIf(feature -> feature.name().equals("dynamicRendering"));
        REQUIRED_FEATURESET = new FeatureSet(required.name(), extensions, features, required.condition());
    }

    @Inject(method = "optionalFeatureSets", at = @At("RETURN"))
    private static void polydium$enableWhenPresent(CallbackInfoReturnable<Set<FeatureSet>> cir) {
        if (VulkanLegacyRendering.FORCE) {
            return;
        }
        cir.getReturnValue().add(new FeatureSet(
            "KHR Dynamic Rendering", Set.of("VK_KHR_dynamic_rendering"), Set.of(new VulkanFeature(DYNAMIC_RENDERING_FEATURES_STRUCT, "dynamicRendering"))
        ));
        cir.getReturnValue().add(new FeatureSet("KHR Push Descriptor", Set.of("VK_KHR_push_descriptor"), Set.of()));
    }
}
//?} else if >= 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vulkan.VulkanBackend;
import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import com.mojang.blaze3d.vulkan.init.VulkanFeature;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.polyfrost.polydium.client.perf.VulkanLegacyRendering;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// 26.4-snapshot-1 no longer requires VK_KHR_dynamic_rendering or VK_KHR_push_descriptor; they're enabled only when present
@Mixin(VulkanBackend.class)
public abstract class MixinVulkanLegacyFeatures {
    @Shadow
    @Final
    @Mutable
    public static Set<String> REQUIRED_DEVICE_EXTENSIONS;

    @Shadow
    @Final
    @Mutable
    public static Set<VulkanFeature> REQUIRED_DEVICE_FEATURES;

    @Unique
    private static VulkanFeature polydium$dynamicRenderingFeature;

    @Shadow
    private static boolean isFeatureSupported(VkPhysicalDevice vkPhysicalDevice, VulkanFeature feature) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void polydium$relaxRequired(CallbackInfo ci) {
        Set<String> extensions = new HashSet<>(REQUIRED_DEVICE_EXTENSIONS);
        extensions.remove("VK_KHR_dynamic_rendering");
        extensions.remove("VK_KHR_push_descriptor");
        REQUIRED_DEVICE_EXTENSIONS = Set.copyOf(extensions);
        Set<VulkanFeature> features = new HashSet<>();
        for (VulkanFeature feature : REQUIRED_DEVICE_FEATURES) {
            if (feature.name().equals("dynamicRendering")) {
                polydium$dynamicRenderingFeature = feature;
            } else {
                features.add(feature);
            }
        }
        REQUIRED_DEVICE_FEATURES = Set.copyOf(features);
    }

    @WrapOperation(
        method = "createDevice(JLcom/mojang/blaze3d/shaders/ShaderSource;Lcom/mojang/blaze3d/shaders/GpuDebugOptions;Ljava/lang/Runnable;)Lcom/mojang/blaze3d/systems/GpuDevice;",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanBackend;createDevice(Ljava/util/Collection;Lcom/mojang/blaze3d/vulkan/VulkanPhysicalDevice;Ljava/util/Set;)Lorg/lwjgl/vulkan/VkDevice;")
    )
    private VkDevice polydium$enableWhenPresent(Collection<String> extensions, VulkanPhysicalDevice physicalDevice, Set<VulkanFeature> features, Operation<VkDevice> original) {
        if (!VulkanLegacyRendering.FORCE) {
            if (physicalDevice.hasDeviceExtension("VK_KHR_dynamic_rendering") && isFeatureSupported(physicalDevice.vkPhysicalDevice(), polydium$dynamicRenderingFeature)) {
                extensions.add("VK_KHR_dynamic_rendering");
                features.add(polydium$dynamicRenderingFeature);
            }
            if (physicalDevice.hasDeviceExtension("VK_KHR_push_descriptor")) {
                extensions.add("VK_KHR_push_descriptor");
            }
        }
        return original.call(extensions, physicalDevice, features);
    }
}
*///?}
