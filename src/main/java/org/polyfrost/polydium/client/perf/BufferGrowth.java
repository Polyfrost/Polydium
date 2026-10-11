package org.polyfrost.polydium.client.perf;

public final class BufferGrowth {
    private BufferGrowth() {
    }

    public static long growth(long capacity, long vanilla) {
        return Math.max(vanilla, capacity >> 1);
    }

    public static int growth(int capacity, int vanilla) {
        return Math.max(vanilla, Math.min(capacity >> 1, Integer.MAX_VALUE - capacity));
    }
}
