package org.polyfrost.polydium.mixin.client.perf.punchy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "punchy.compat.IrisShaderPackCompat", remap = false)
public class MixinPunchyIrisShaderPackCache {
    @Unique
    private static final long POLYDIUM$REFRESH_NANOS = 50_000_000L;
    @Unique
    private static long polydium$checkedAt;
    @Unique
    private static boolean polydium$hasValue;
    @Unique
    private static boolean polydium$shaderPackInUse;

    @WrapMethod(method = "isShaderPackInUse", remap = false, require = 0)
    private static boolean polydium$cachedShaderPackInUse(Operation<Boolean> original) {
        long now = System.nanoTime();
        if (!polydium$hasValue || now - polydium$checkedAt >= POLYDIUM$REFRESH_NANOS) {
            polydium$shaderPackInUse = original.call();
            polydium$checkedAt = now;
            polydium$hasValue = true;
        }
        return polydium$shaderPackInUse;
    }
}
