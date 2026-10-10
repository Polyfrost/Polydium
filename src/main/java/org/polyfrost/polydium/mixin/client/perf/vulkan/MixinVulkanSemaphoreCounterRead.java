package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanUtils;
import org.lwjgl.vulkan.VK12;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "awaitSubmitCompletion(JJ)Z", at = @At("HEAD"), cancellable = true)
    private void polydium$readSubmitCounter(long submitIndex, long timeout, CallbackInfoReturnable<Boolean> cir) {
        if (timeout != 0L || submitIndex <= completedSubmitIndex || submitIndex >= currentSubmitIndex) {
            return;
        }
        int result = VK12.vkGetSemaphoreCounterValue(device.vkDevice(), submitSemaphore, polydium$counterValue);
        VulkanUtils.crashIfFailure(device, result, "Failed to wait for semaphore");
        boolean done = polydium$counterValue[0] >= submitIndex;
        if (done) {
            completedSubmitIndex = submitIndex;
        }
        cir.setReturnValue(done);
    }
}
//?}
