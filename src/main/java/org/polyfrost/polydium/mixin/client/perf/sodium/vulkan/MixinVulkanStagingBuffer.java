package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import net.caffeinemc.mods.sodium.client.gl.arena.staging.StagingBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.sodium.VkStagingBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderRegionManager.class, remap = false)
public class MixinVulkanStagingBuffer {

    @Inject(method = "createStagingBuffer", at = @At("HEAD"), cancellable = true)
    private static void polydium$vulkanStaging(CommandList commandList, CallbackInfoReturnable<StagingBuffer> cir) {
        if (VulkanActive.active)
            cir.setReturnValue(new VkStagingBuffer());
    }
}
