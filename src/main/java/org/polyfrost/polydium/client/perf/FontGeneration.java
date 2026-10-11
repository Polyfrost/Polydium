package org.polyfrost.polydium.client.perf;

public final class FontGeneration {
    private static volatile int generation;

    private FontGeneration() {
    }

    public static int get() {
        return generation;
    }

    public static void bump() {
        generation++;
    }
}
