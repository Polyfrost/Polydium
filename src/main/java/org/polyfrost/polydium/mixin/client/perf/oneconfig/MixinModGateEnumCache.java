package org.polyfrost.polydium.mixin.client.perf.oneconfig;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Pseudo
@Mixin(targets = "org.polyfrost.oneconfig.internal.compat.toggle.ModGates", remap = false)
public abstract class MixinModGateEnumCache {
    @Unique
    private static final Map<String, Map<String, Object>> polydium$constants = new ConcurrentHashMap<>();

    @WrapMethod(method = "enumConstant", require = 0)
    private static Object polydium$cachedEnumConstant(String className, String name, Operation<Object> original) {
        Map<String, Object> byName = polydium$constants.computeIfAbsent(className, k -> new ConcurrentHashMap<>());
        Object constant = byName.get(name);
        if (constant == null && (constant = original.call(className, name)) != null) {
            byName.put(name, constant);
        }
        return constant;
    }
}
