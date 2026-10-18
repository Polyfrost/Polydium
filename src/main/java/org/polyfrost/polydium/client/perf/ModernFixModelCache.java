package org.polyfrost.polydium.client.perf;

import net.minecraft.world.level.block.state.BlockState;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class ModernFixModelCache {
    private static final MethodHandle RESET = find();

    private ModernFixModelCache() {
    }

    public static void reset() {
        if (RESET == null) {
            return;
        }
        try {
            RESET.invokeExact();
        } catch (Throwable ignored) {
        }
    }

    private static MethodHandle find() {
        try {
            if (!Class.forName("org.embeddedt.modernfix.duck.IModelHoldingBlockState").isAssignableFrom(BlockState.class)) {
                return null;
            }
            Class<?> map = Class.forName("org.embeddedt.modernfix.dynresources.BlockStateModelMap");
            return MethodHandles.publicLookup().findStatic(map, "resetCache", MethodType.methodType(void.class));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }
}
