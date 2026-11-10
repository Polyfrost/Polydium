package org.polyfrost.polydium.client.perf;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public final class CallbackInfoPool {
    private static final VarHandle CANCELLED = cancelledHandle();
    private static final CallbackInfo[] POOL = new CallbackInfo[64];
    private static int depth;

    private CallbackInfoPool() {
    }

    private static VarHandle cancelledHandle() {
        try {
            return MethodHandles.privateLookupIn(CallbackInfo.class, MethodHandles.lookup()).findVarHandle(CallbackInfo.class, "cancelled", boolean.class);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    public static CallbackInfo acquire(String id) {
        if (CANCELLED == null || depth == POOL.length || !RenderSystem.isOnRenderThread())
            return new CallbackInfo(id, true);

        CallbackInfo info = POOL[depth];
        if (info == null || info.getId() != id)
            POOL[depth] = info = new CallbackInfo(id, true);
        else
            CANCELLED.set(info, false);
        depth++;
        return info;
    }

    public static boolean release(CallbackInfo info) {
        if (depth > 0 && POOL[depth - 1] == info)
            depth--;
        return info.isCancelled();
    }
}
