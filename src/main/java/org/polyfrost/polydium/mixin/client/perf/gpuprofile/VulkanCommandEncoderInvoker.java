package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VulkanCommandEncoder.class)
public interface VulkanCommandEncoderInvoker {
    @Invoker("endCommandBuffer")
    void polydium$endCommandBuffer();

    @Accessor("currentCommandBuffer")
    @Nullable VkCommandBuffer polydium$currentCommandBuffer();
}
//?}
