package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@Mixin(targets = "net.minecraft.client.renderer.StagedVertexBuffer$GpuBufferPool")
public class MixinOrderedRecyclePolling {
    @WrapOperation(
        method = "tryRecycleBuffers",
        at = @At(value = "INVOKE", target = "Ljava/util/List;removeIf(Ljava/util/function/Predicate;)Z")
    )
    private boolean polydium$recycleInOrder(List<Object> pending, Predicate<Object> tryRecycle, Operation<Boolean> original) {
        boolean removed = false;
        for (Iterator<Object> it = pending.iterator(); it.hasNext(); ) {
            if (!tryRecycle.test(it.next())) {
                break;
            }
            it.remove();
            removed = true;
        }
        return removed;
    }
}
//?}
