package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 && < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.polyfrost.polydium.client.perf.BatchOrder;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Mixin(ModelFeatureRenderer.class)
public class MixinBatchPipelineSortModels {
    @WrapOperation(method = "renderBatch", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;"))
    private Iterator<Object> polydium$orderByPipeline(Iterable<Object> entries, Operation<Iterator<Object>> original) {
        if (!(entries instanceof Collection<Object> batches) || batches.size() < 2 /^? if >= 1.21.11 {^/|| IrisShaders.packInUse()/^?}^/) {
            return original.call(entries);
        }
        List<Object> ordered = new ArrayList<>(batches);
        ordered.sort(BatchOrder.BY_PIPELINE);
        return ordered.iterator();
    }
}
*///?}
