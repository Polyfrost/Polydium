package org.polyfrost.polydium.client.perf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

public final class SodiumCullTreePool {
    private static final int TREE_LONGS = 4096;
    private static final int MAX_POOLED = 64;
    private static final ThreadLocal<ArrayList<long[]>> COLLECTING = new ThreadLocal<>();
    private static final ArrayDeque<long[]> FREE = new ArrayDeque<>();

    public interface Owner {
        ArrayList<long[]> polydium$treeArrays();
    }

    private SodiumCullTreePool() {
    }

    public static void begin(ArrayList<long[]> owned) {
        COLLECTING.set(owned);
    }

    public static void end() {
        COLLECTING.remove();
    }

    public static boolean collecting() {
        return COLLECTING.get() != null;
    }

    public static long[] take() {
        long[] array;
        synchronized (FREE) {
            array = FREE.pollLast();
        }
        if (array == null) {
            array = new long[TREE_LONGS];
        } else {
            Arrays.fill(array, 0L);
        }
        COLLECTING.get().add(array);
        return array;
    }

    public static void release(ArrayList<long[]> arrays) {
        synchronized (FREE) {
            for (int i = 0, n = Math.min(arrays.size(), MAX_POOLED - FREE.size()); i < n; i++) {
                FREE.addLast(arrays.get(i));
            }
        }
        arrays.clear();
    }
}
