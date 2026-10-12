package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 26.2 {
import net.minecraft.client.renderer.feature.QuadParticleFeatureRenderer;
//?} else if >= 26.1 {
/*import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
*///?} else {
/*import net.minecraft.client.renderer.state.QuadParticleRenderState;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 26.2 {
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
//?} else {
/*
import java.util.HashMap;
*///?}

//? if >= 26.2 {
@Mixin(QuadParticleFeatureRenderer.class)
public abstract class MixinParticleDrawMapPool {
    @Unique
    private static final int POLYDIUM_UNRESIZED_ENTRIES = 21;

    @Unique
    private final List<IdentityHashMap<?, ?>> polydium$freeMaps = new ArrayList<>();
    @Unique
    private final List<IdentityHashMap<?, ?>> polydium$lentMaps = new ArrayList<>();

    @WrapOperation(method = "prepareGroup", at = @At(value = "NEW", target = "()Ljava/util/IdentityHashMap;"))
    private IdentityHashMap<?, ?> polydium$lendMap(Operation<IdentityHashMap<?, ?>> original) {
        IdentityHashMap<?, ?> map = this.polydium$freeMaps.isEmpty() ? original.call() : this.polydium$freeMaps.removeLast();
        this.polydium$lentMaps.add(map);
        return map;
    }

    @Inject(method = "finishExecute", at = @At("HEAD"))
    private void polydium$returnMaps(CallbackInfo ci) {
        List<IdentityHashMap<?, ?>> lent = this.polydium$lentMaps;
        for (int i = 0; i < lent.size(); i++) {
            IdentityHashMap<?, ?> map = lent.get(i);
            if (map.size() > POLYDIUM_UNRESIZED_ENTRIES) {
                continue;
            }
            if (!map.isEmpty()) {
                map.clear();
            }
            this.polydium$freeMaps.add(map);
        }
        lent.clear();
    }
}
//?} else {
/*@Mixin(QuadParticleRenderState.class)
public abstract class MixinParticleDrawMapPool {
    @Unique
    private static final int POLYDIUM_UNRESIZED_ENTRIES = 12;

    @Unique
    private HashMap<?, ?> polydium$layers;

    @WrapOperation(method = "prepare", at = @At(value = "NEW", target = "()Ljava/util/HashMap;"))
    private HashMap<?, ?> polydium$reuseMap(Operation<HashMap<?, ?>> original) {
        HashMap<?, ?> map = this.polydium$layers;
        if (map == null || map.size() > POLYDIUM_UNRESIZED_ENTRIES) {
            return this.polydium$layers = original.call();
        }
        map.clear();
        return map;
    }
}
*///?}
//?}
