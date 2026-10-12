package org.polyfrost.polydium.client.perf;

public final class SpriteMarkVersion {
    private static long version;

    private SpriteMarkVersion() {
    }

    public static long current() {
        return version;
    }

    public static void bump() {
        version++;
    }
}
