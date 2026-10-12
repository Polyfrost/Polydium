package org.polyfrost.polydium.mixin.client.perf.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import org.polyfrost.polydium.client.perf.SodiumRegionPassCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion", remap = false)
public abstract class MixinRegionPassArrays {
    @Unique
    private final SodiumRegionPassCache polydium$storages = new SodiumRegionPassCache();
    @Unique
    private final SodiumRegionPassCache polydium$batches = new SodiumRegionPassCache();

    @WrapOperation(
            method = "getStorage",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object polydium$storageByPass(Map<?, ?> map, Object pass, Operation<Object> original) {
        return this.polydium$lookup(this.polydium$storages, map, pass, original);
    }

    //? if >= 1.21.8 || < 1.21.4 {
    @WrapOperation(
            method = "getCachedBatch",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object polydium$batchByPass(Map<?, ?> map, Object pass, Operation<Object> original) {
        return this.polydium$lookup(this.polydium$batches, map, pass, original);
    }
    //?}

    @Inject(method = "delete", at = @At("TAIL"))
    private void polydium$forgetPasses(CallbackInfo ci) {
        this.polydium$storages.clear();
        this.polydium$batches.clear();
    }

    @Unique
    private Object polydium$lookup(SodiumRegionPassCache cache, Map<?, ?> map, Object pass, Operation<Object> original) {
        int index = pass == DefaultTerrainRenderPasses.SOLID ? 0
                : pass == DefaultTerrainRenderPasses.CUTOUT ? 1
                : pass == DefaultTerrainRenderPasses.TRANSLUCENT ? 2
                : -1;
        if (index < 0) {
            return original.call(map, pass);
        }
        Object[] slots = cache.slotsFor(map);
        Object value = slots[index];
        if (value == null) {
            slots[index] = value = original.call(map, pass);
        }
        return value;
    }
}
