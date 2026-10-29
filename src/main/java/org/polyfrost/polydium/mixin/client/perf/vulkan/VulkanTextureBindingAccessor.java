package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 && < 26.3 {
/*import com.mojang.blaze3d.vulkan.VulkanGpuSampler;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.mojang.renderpearl.backend.vulkan.VulkanRenderPass$TextureViewAndSampler")
public interface VulkanTextureBindingAccessor {
    @Accessor("view")
    VulkanGpuTextureView polydium$view();

    @Accessor("sampler")
    VulkanGpuSampler polydium$sampler();
}
*///?}
