package org.polyfrost.polydium.mixin.client.perf.immediatelyfast;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
//? if >= 1.21.11 && < 26.1 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import org.polyfrost.polydium.client.perf.BufferSetPool;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
*///?}

@Pseudo
@Mixin(targets = "net.raphimc.immediatelyfast.feature.core.BatchableBufferSource", remap = false)
public abstract class MixinBatchableBufferSetPool {
    //? if >= 1.21.11 && < 26.1 {
    /*@Unique
    private final BufferSetPool polydium$sets = new BufferSetPool();

    @ModifyArg(method = "method_73477", at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"), index = 1, remap = false, require = 0)
    private Function<Object, Object> polydium$pooledSetFactory(Function<Object, Object> factory) {
        return this.polydium$sets;
    }

    @ModifyExpressionValue(method = "drawDirect", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"), remap = false, require = 0)
    private boolean polydium$releaseDrawnSet(boolean hasNext, @Local Set<?> buffers) {
        if (!hasNext) {
            this.polydium$sets.release(buffers);
        }
        return hasNext;
    }

    @ModifyReceiver(method = "close", at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V"), remap = false, require = 0)
    private Map<?, ?> polydium$releaseClearedSets(Map<?, ?> dynamicBuffers) {
        if (!dynamicBuffers.isEmpty()) {
            for (Object buffers : dynamicBuffers.values()) {
                this.polydium$sets.release(buffers);
            }
        }
        return dynamicBuffers;
    }
    *///?}
}
