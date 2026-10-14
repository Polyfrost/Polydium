package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;

@Mixin(value = Minecraft.class, priority = 1500)
public class MixinVulkanRenderAhead {

    @Unique
    private final ArrayDeque<GpuFence> polydium$fences = new ArrayDeque<>();

    @Inject(method = "runTick", at = @At("HEAD"))
    private void polydium$waitForGpu(boolean tick, CallbackInfo ci) {
        if (!VulkanActive.active)
            return;

        int limit = Math.max(1, SodiumClientMod.options().advanced.cpuRenderAheadLimit);

        while (this.polydium$fences.size() > limit) {
            try (GpuFence fence = this.polydium$fences.removeFirst()) {
                fence.awaitCompletion(Long.MAX_VALUE);
            }
        }
    }

    @Unique
    private long polydium$fencedSubmit;

    @Inject(method = "runTick", at = @At("RETURN"))
    private void polydium$fenceFrame(boolean tick, CallbackInfo ci) {
        if (!VulkanActive.active)
            return;

        long submit = VulkanDevice.active.createCommandEncoder().currentSubmitIndex();
        if (submit == this.polydium$fencedSubmit)
            return;

        this.polydium$fencedSubmit = submit;
        this.polydium$fences.addLast(RenderSystem.getDevice().createCommandEncoder().createFence());
    }
}
