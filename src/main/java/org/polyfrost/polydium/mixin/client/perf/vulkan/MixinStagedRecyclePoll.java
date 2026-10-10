package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.GpuFence;
import org.polyfrost.polydium.client.perf.RecyclePolling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.renderer.StagedVertexBuffer$GpuBufferPool$PendingRecycle")
public class MixinStagedRecyclePoll {
    @WrapOperation(
        method = "tryRecycle()Ljava/util/List;",
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/GpuFence;awaitCompletion(J)Z")
    )
    private boolean polydium$skipKnownPending(GpuFence fence, long timeout, Operation<Boolean> original) {
        if (!(fence instanceof MixinVulkanFencePollAccessor vulkanFence)) {
            return original.call(fence, timeout);
        }
        long submitIndex = vulkanFence.polydium$submitIndex();
        if (RecyclePolling.isKnownPending(submitIndex)) {
            return false;
        }
        boolean done = original.call(fence, timeout);
        if (!done) {
            RecyclePolling.markPending(submitIndex);
        }
        return done;
    }
}
//?}
