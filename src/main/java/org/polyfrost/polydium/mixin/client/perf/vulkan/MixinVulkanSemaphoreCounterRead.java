package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanUtils;
import org.lwjgl.vulkan.VK12;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VulkanCommandEncoder.class)
public class MixinVulkanSemaphoreCounterRead {
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
    private final long[] polydium$counterValue = new long[1];

    @WrapMethod(method = "awaitSubmitCompletion(JJ)Z")
    private boolean polydium$readSubmitCounter(long submitIndex, long timeout, Operation<Boolean> original) {
        if (timeout != 0L || submitIndex <= completedSubmitIndex || submitIndex >= currentSubmitIndex) {
            return original.call(submitIndex, timeout);
        }
        int result = VK12.vkGetSemaphoreCounterValue(device.vkDevice(), submitSemaphore, polydium$counterValue);
        VulkanUtils.crashIfFailure(device, result, "Failed to wait for semaphore");
        boolean done = polydium$counterValue[0] >= submitIndex;
        if (done) {
            completedSubmitIndex = submitIndex;
        }
        return done;
    }
}
//?}
