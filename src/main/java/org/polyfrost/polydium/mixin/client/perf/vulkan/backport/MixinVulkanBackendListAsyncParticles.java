package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
    "fun.qu_an.minecraft.asyncparticles.client.core.backend.BackendCaps",
    "fun.qu_an.minecraft.asyncparticles.client.core.backend.Backends",
    "fun.qu_an.minecraft.asyncparticles.client.core.particle.gpu_acceleration.vulkan.VkCompParticleRenderer",
    "fun.qu_an.minecraft.asyncparticles.client.core.particle.gpu_acceleration.vulkan.VkCompParticleRenderer$SourceSlot",
    "fun.qu_an.minecraft.asyncparticles.client.core.particle.gpu_acceleration.vulkan.VkCompParticleRenderer$SubmitSlot",
    "fabric.fun.qu_an.minecraft.asyncparticles.client.core.backend.Backends",
    "fabric.fun.qu_an.minecraft.asyncparticles.client.core.particle.gpu_acceleration.vulkan.VkCompParticleRenderer",
    "fabric.fun.qu_an.minecraft.asyncparticles.client.core.particle.gpu_acceleration.vulkan.VkCompParticleRenderer$SourceSlot",
    "fabric.fun.qu_an.minecraft.asyncparticles.client.core.particle.gpu_acceleration.vulkan.VkCompParticleRenderer$SubmitSlot"
}, remap = false)
public class MixinVulkanBackendListAsyncParticles {
}
