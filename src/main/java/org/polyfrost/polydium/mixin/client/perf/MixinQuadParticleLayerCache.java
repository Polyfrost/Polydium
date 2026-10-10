package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
//?} else {
/*import net.minecraft.client.renderer.state.QuadParticleRenderState;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.function.Function;

@Mixin(QuadParticleRenderState.class)
public class MixinQuadParticleLayerCache {
    @Unique
    private static final int POLYDIUM_CACHED_LAYERS = 8;

    @Unique
    private final Object[] polydium$layers = new Object[POLYDIUM_CACHED_LAYERS];
    @Unique
    private final Object[] polydium$storages = new Object[POLYDIUM_CACHED_LAYERS];
    @Unique
    private int polydium$cachedLayers;

    @WrapOperation(
            method = "add",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;")
    )
    private Object polydium$reuseLayerStorage(Map<Object, Object> particles, Object layer, Function<Object, Object> factory, Operation<Object> original) {
        int count = polydium$cachedLayers;
        for (int i = 0; i < count; i++) {
            if (polydium$layers[i] == layer) {
                return polydium$storages[i];
            }
        }

        Object storage = original.call(particles, layer, factory);
        if (count < POLYDIUM_CACHED_LAYERS) {
            polydium$layers[count] = layer;
            polydium$storages[count] = storage;
            polydium$cachedLayers = count + 1;
        }
        return storage;
    }
}
//?}
