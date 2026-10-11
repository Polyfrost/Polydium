package org.polyfrost.polydium.client.perf;

import java.util.concurrent.ConcurrentHashMap;

public final class CutoutTypeCache {
    private static final ConcurrentHashMap<Object, Object> TYPES = new ConcurrentHashMap<>();
    private static int generation = FontGeneration.get();

    private CutoutTypeCache() {
    }

    public static Object get(Object texture) {
        int current = FontGeneration.get();
        if (current != generation) {
            generation = current;
            TYPES.clear();
        }
        return TYPES.get(texture);
    }

    public static void put(Object texture, Object type) {
        TYPES.put(texture, type);
    }

    public static void clear() {
        TYPES.clear();
    }
}
