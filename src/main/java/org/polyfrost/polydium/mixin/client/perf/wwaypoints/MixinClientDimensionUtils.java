package org.polyfrost.polydium.mixin.client.perf.wwaypoints;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Pseudo
@Mixin(targets = "com.wwaypoints.client.util.ClientDimensionUtils", remap = false)
public class MixinClientDimensionUtils {
    @Unique
    private static Map.Entry<String, String> polydium$last;

    @WrapMethod(method = "canonicalize", remap = false)
    private static String polydium$reuseLast(String raw, Operation<String> original) {
        Map.Entry<String, String> last = polydium$last;
        if (last != null && last.getKey().equals(raw)) return last.getValue();
        String result = original.call(raw);
        if (raw != null) polydium$last = Map.entry(raw, result);
        return result;
    }
}
