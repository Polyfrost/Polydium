package org.polyfrost.polydium.mixin.client.perf.wwaypoints;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Pseudo
@Mixin(targets = "com.wwaypoints.client.HideWaypointCycle", remap = false)
public class MixinHideWaypointCycle {
    @Unique
    private static int[] polydium$lastRaw;
    @Unique
    private static int polydium$lastLegacy;
    @Unique
    private static int[] polydium$lastCycle;

    @WrapOperation(method = "activeThreshold", at = @At(value = "INVOKE", target = "Lcom/wwaypoints/client/HideWaypointCycle;normalize([II)[I"), remap = false)
    private static int[] polydium$reuseCycle(int[] raw, int legacy, Operation<int[]> original) {
        if (polydium$lastCycle != null && legacy == polydium$lastLegacy && Arrays.equals(raw, polydium$lastRaw)) return polydium$lastCycle;
        polydium$lastRaw = raw == null ? null : raw.clone();
        polydium$lastLegacy = legacy;
        return polydium$lastCycle = original.call(raw, legacy);
    }
}
