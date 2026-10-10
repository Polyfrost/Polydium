package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FeatureRendererMap.class)
public class MixinFeatureRendererList {
    @Unique
    private List<FeatureRenderer<?>> polydium$values;

    @Inject(method = "put", at = @At("RETURN"))
    private void polydium$invalidateValues(CallbackInfo ci) {
        this.polydium$values = null;
    }

    @WrapMethod(method = "values")
    private Iterable<FeatureRenderer<?>> polydium$cachedValues(Operation<Iterable<FeatureRenderer<?>>> original) {
        List<FeatureRenderer<?>> values = this.polydium$values;
        if (values == null) {
            this.polydium$values = values = ImmutableList.copyOf(original.call());
        }
        return values;
    }
}
//?}
