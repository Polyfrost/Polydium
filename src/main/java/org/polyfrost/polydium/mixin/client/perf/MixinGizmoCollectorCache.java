package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.gizmos.Gizmos;
import org.polyfrost.polydium.client.perf.GizmoCollectorCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gizmos.class)
public class MixinGizmoCollectorCache {
    @WrapOperation(method = "addGizmo", at = @At(value = "INVOKE", target = "Ljava/lang/ThreadLocal;get()Ljava/lang/Object;"))
    private static Object polydium$cachedCollector(ThreadLocal<?> collector, Operation<Object> original) {
        return GizmoCollectorCache.get(collector);
    }

    @WrapOperation(method = "withCollector", at = @At(value = "INVOKE", target = "Ljava/lang/ThreadLocal;set(Ljava/lang/Object;)V"))
    private static void polydium$setCollector(ThreadLocal<?> collector, Object value, Operation<Void> original) {
        original.call(collector, value);
        GizmoCollectorCache.set(value);
    }
}
//?}
