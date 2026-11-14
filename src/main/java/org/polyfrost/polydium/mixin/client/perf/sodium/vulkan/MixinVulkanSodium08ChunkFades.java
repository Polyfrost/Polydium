package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gl.buffer.GlBufferStreamer", remap = false)
public class MixinVulkanSodium08ChunkFades {
    @Shadow
    private boolean requiresFlush;

    // Sodium only uploads its zeroed chunk fade table once a fade time is written. GL drivers zero new buffers, but
    // Vulkan buffers start out with leftover memory, so regions recreated by already-built sections (e.g. a crater
    // into solid ground) would otherwise read garbage fade times and render entirely in the fog color.
    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$uploadInitialFades(CallbackInfo ci) {
        if (VulkanActive.active) {
            this.requiresFlush = true;
        }
    }
}
