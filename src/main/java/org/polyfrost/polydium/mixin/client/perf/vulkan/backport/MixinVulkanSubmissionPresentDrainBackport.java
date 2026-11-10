package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.client.perf.PresentThread;
import org.polyfrost.polydium.vulkan.VulkanQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanQueue.Submission.class)
public abstract class MixinVulkanSubmissionPresentDrainBackport {
    @Inject(method = "close", at = @At("HEAD"))
    private void polydium$drainPresentThread(CallbackInfo ci) {
        PresentThread.drain();
    }
}
