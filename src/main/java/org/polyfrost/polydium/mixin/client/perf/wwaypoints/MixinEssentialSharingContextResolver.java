package org.polyfrost.polydium.mixin.client.perf.wwaypoints;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.wwaypoints.client.sharing.EssentialSharingContextResolver", remap = false)
public abstract class MixinEssentialSharingContextResolver {
    @Unique
    private static final Map<Class<?>, Map<String, Object>> polydium$lookups = new ConcurrentHashMap<>();

    @Unique
    private static volatile Object polydium$essential;

    @WrapOperation(method = "resolve", at = @At(value = "INVOKE", target = "Ljava/lang/Class;forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"), require = 0)
    private static Class<?> polydium$cachedEssential(String name, boolean initialize, ClassLoader loader, Operation<Class<?>> original) throws ClassNotFoundException {
        Object cached = polydium$essential;
        if (cached == null) {
            try {
                cached = original.call(name, initialize, loader);
            } catch (Exception e) {
                if (!(e instanceof ClassNotFoundException))
                    throw e;
                cached = e;
            }
            polydium$essential = cached;
        }
        if (cached instanceof ClassNotFoundException e)
            throw e;
        return (Class<?>) cached;
    }

    @WrapOperation(method = {"resolve", "identityFromSession"}, at = @At(value = "INVOKE", target = "Ljava/lang/Class;getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"), require = 0)
    private static Method polydium$cachedGetMethod(Class<?> type, String name, Class<?>[] parameters, Operation<Method> original) throws NoSuchMethodException {
        Map<String, Object> lookups = polydium$lookups.computeIfAbsent(type, k -> new ConcurrentHashMap<>());
        Object cached = lookups.get(name);
        if (cached == null) {
            try {
                cached = original.call(type, name, parameters);
            } catch (Exception e) {
                if (!(e instanceof NoSuchMethodException))
                    throw e;
                cached = e;
            }
            lookups.put(name, cached);
        }
        if (cached instanceof NoSuchMethodException e)
            throw e;
        return (Method) cached;
    }
}
