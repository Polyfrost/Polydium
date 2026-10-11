package org.polyfrost.polydium.client.perf;

import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.List;

public final class SkinLayerMeshes {
    public static final int QUAD_FLOATS = 23;

    private static final MethodHandle POLYGONS;
    private static final MethodHandle CUBES;
    private static final MethodHandle PROVIDER;
    private static final Object EMPTY_PROVIDER;

    static {
        MethodHandle polygons = null;
        MethodHandle cubes = null;
        MethodHandle provider = null;
        Object emptyProvider = null;
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> part = Class.forName("dev.tr7zw.skinlayers.render.CustomizableModelPart");
            polygons = getter(lookup, part.getSuperclass(), "polygonData", float[].class);
            cubes = getter(lookup, part, "cubes", List.class);
            provider = lookup.unreflect(Class.forName("dev.tr7zw.skinlayers.api.SkinLayersAPI").getMethod("getMeshTransformerProvider"))
                    .asType(MethodType.methodType(Object.class));
            emptyProvider = Class.forName("dev.tr7zw.skinlayers.api.MeshTransformerProvider").getField("EMPTY_PROVIDER").get(null);
        } catch (Throwable t) {
            LoggerFactory.getLogger("polydium/perf").info("Skin layer batch writes disabled: {}", t.toString());
            polygons = null;
        }
        POLYGONS = polygons;
        CUBES = cubes;
        PROVIDER = provider;
        EMPTY_PROVIDER = emptyProvider;
    }

    private SkinLayerMeshes() {
    }

    private static MethodHandle getter(MethodHandles.Lookup lookup, Class<?> owner, String name, Class<?> type) throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return lookup.unreflectGetter(field).asType(MethodType.methodType(type, Object.class));
    }

    public static float[] polygons(Object mesh) {
        if (POLYGONS == null) {
            return null;
        }
        try {
            if ((Object) PROVIDER.invokeExact() != EMPTY_PROVIDER || !((List<?>) CUBES.invokeExact(mesh)).isEmpty()) {
                return null;
            }
            float[] data = (float[]) POLYGONS.invokeExact(mesh);
            return data != null && data.length % QUAD_FLOATS == 0 ? data : null;
        } catch (Throwable t) {
            return null;
        }
    }
}
