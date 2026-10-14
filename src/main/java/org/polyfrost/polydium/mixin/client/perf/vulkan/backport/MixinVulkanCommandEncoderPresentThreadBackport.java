package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.polyfrost.polydium.client.perf.FramePath;
import org.polyfrost.polydium.client.perf.PresentThread;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanCommandEncoderPresentThreadBackport {
    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanCommandEncoder;awaitSubmitCompletion(JJ)Z"))
    private boolean polydium$measureGpuWait(VulkanCommandEncoder encoder, long submitIndex, long timeout, Operation<Boolean> original) {
        long    start  = System.nanoTime();
        boolean result = original.call(encoder, submitIndex, timeout);
        FramePath.recordGpuWait(System.nanoTime() - start);
        return result;
    }

    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lorg/polyfrost/polydium/vulkan/VulkanQueue$Submission;close()V"))
    private void polydium$submitOnPresentThread(VulkanQueue.Submission submission, Operation<Void> original) {
        if (!PresentThread.isArmed()) {
            original.call(submission);
            return;
        }
        PresentThread.handOff(submission);
    }
}
