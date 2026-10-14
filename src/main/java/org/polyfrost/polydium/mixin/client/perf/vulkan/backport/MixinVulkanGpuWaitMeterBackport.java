package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.polyfrost.polydium.client.perf.GpuWaitMeter;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanGpuWaitMeterBackport {
    @Inject(method = "awaitSubmitCompletion(JJ)Z", at = @At("HEAD"))
    private void polydium$beginGpuWait(long submitIndex, long timeout, CallbackInfoReturnable<Boolean> cir) {
        if (timeout != 0L) {
            GpuWaitMeter.beginWait();
        }
    }

    @Inject(method = "awaitSubmitCompletion(JJ)Z", at = @At("RETURN"))
    private void polydium$endGpuWait(long submitIndex, long timeout, CallbackInfoReturnable<Boolean> cir) {
        if (timeout != 0L) {
            GpuWaitMeter.endWait();
        }
    }
}
