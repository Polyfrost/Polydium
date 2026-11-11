package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.vulkan.VulkanActive;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// RawGl already keeps raw OpenGL calls from crashing; these skip whole routines that would only draw its warnings
@Pseudo
@Mixin(targets = {
    "fudge.notenoughcrashes.utils.GlUtil",
    "me.cortex.voxy.client.VoxyClient"
}, remap = false)
public class MixinVulkanModGl {

    @Inject(method = "resetState", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$skipGlReset(CallbackInfo ci) {
        if (VulkanActive.active) ci.cancel();
    }

    @Inject(method = "initVoxyClient", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$skipVoxy(CallbackInfo ci) {
        if (!VulkanActive.active) return;
        LoggerFactory.getLogger("polydium/vulkan").warn("Voxy only renders through OpenGL; disabled while the Vulkan backend is active");
        ci.cancel();
    }
}
