package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.minecraft.client.model.geom.ModelPart;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public final class SodiumModelParts {
    private static final VarHandle CUBOID = find(ModelPart.Cube.class, "sodium$cuboid", ModelCuboid.class);
    private static final VarHandle VALUES = find(Object2ObjectArrayMap.class, "value", Object[].class);
    private static final VarHandle SIZE = find(Object2ObjectArrayMap.class, "size", int.class);

    public static final boolean CUBOIDS = CUBOID != null;
    public static final boolean CHILD_ARRAYS = VALUES != null && SIZE != null;

    private SodiumModelParts() {
    }

    public static ModelCuboid cuboid(ModelPart.Cube cube) {
        return (ModelCuboid) CUBOID.get(cube);
    }

    public static Object[] values(Object2ObjectArrayMap<?, ?> map) {
        return (Object[]) VALUES.get(map);
    }

    public static int size(Object2ObjectArrayMap<?, ?> map) {
        return (int) SIZE.get(map);
    }

    private static VarHandle find(Class<?> owner, String name, Class<?> type) {
        try {
            return MethodHandles.privateLookupIn(owner, MethodHandles.lookup()).findVarHandle(owner, name, type);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
//?}
