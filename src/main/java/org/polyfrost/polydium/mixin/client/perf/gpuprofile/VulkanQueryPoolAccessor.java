package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanQueryPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VulkanQueryPool.class)
public interface VulkanQueryPoolAccessor {
    @Invoker("vkQueryPool")
    long polydium$vkQueryPool();

    @Accessor("device")
    VulkanDevice polydium$device();
}
//?}
