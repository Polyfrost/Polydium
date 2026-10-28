package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.nio.LongBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.vulkan.VK12;
import org.polyfrost.polydium.client.perf.GpuWaitMeter;
import org.polyfrost.polydium.vulkan.VulkanCommandEncoder;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VulkanCommandEncoder.class)
public class MixinVulkanSemaphoreCounterReadBackport {
    @Shadow
    @Final
    private VulkanDevice device;

    @Shadow
    @Final
    private long submitSemaphore;

    @Shadow
    private long currentSubmitIndex;

    @Shadow
    private long completedSubmitIndex;

    @Unique
    private final LongBuffer polydium$counterValue = BufferUtils.createLongBuffer(1);

    @WrapMethod(method = "awaitSubmitCompletion(JJ)Z")
    private boolean polydium$readSubmitCounter(long submitIndex, long timeout, Operation<Boolean> original) {
        if (timeout != 0L) {
            //? if >= 26.1 {
            GpuWaitMeter.beginWait();
            boolean done = original.call(submitIndex, timeout);
            GpuWaitMeter.endWait();
            return done;
            //?} else {
            /*return original.call(submitIndex, timeout);
            *///?}
        }
        if (submitIndex <= completedSubmitIndex || submitIndex >= currentSubmitIndex) {
            return original.call(submitIndex, timeout);
        }

        int result = VK12.vkGetSemaphoreCounterValue(device.vkDevice(), submitSemaphore, polydium$counterValue);
        VulkanUtils.crashIfFailure(device, result, "Failed to wait for semaphore");
        boolean done = polydium$counterValue.get(0) >= submitIndex;
        if (done) {
            completedSubmitIndex = submitIndex;
        }
        return done;
    }
}
