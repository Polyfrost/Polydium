package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VulkanCommandEncoder.class)
public interface VulkanSubmitWaitAccessor {
    @Accessor("currentSubmitIndex")
    long polydium$currentSubmitIndex();

    @Invoker("awaitSubmitCompletion")
    boolean polydium$awaitSubmitCompletion(long submitIndex, long timeoutNS);
}
//?}
