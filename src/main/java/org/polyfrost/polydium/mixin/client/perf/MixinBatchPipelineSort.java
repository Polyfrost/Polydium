package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import org.polyfrost.polydium.client.perf.BatchOrder;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(SimpleFeatureRenderPhase.class)
public class MixinBatchPipelineSort {
    @WrapOperation(method = "sortFeatureInto", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
    private static Collection<Object> polydium$orderByPipeline(Map<Object, Object> batches, Operation<Collection<Object>> original) {
        if (batches.size() < 2 || IrisShaders.packInUse()) {
            return original.call(batches);
        }
        List<Object> ordered = new ArrayList<>(batches.entrySet());
        BatchOrder.sortRuns(ordered, BatchOrder.OPAQUE, BatchOrder.BY_PIPELINE);
        ordered.replaceAll(entry -> ((Map.Entry<?, ?>) entry).getValue());
        return ordered;
    }
}
//?}
