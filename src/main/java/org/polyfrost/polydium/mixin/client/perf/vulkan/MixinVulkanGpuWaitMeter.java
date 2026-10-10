package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.client.perf.GpuWaitMeter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanGpuWaitMeter {
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
//?}
