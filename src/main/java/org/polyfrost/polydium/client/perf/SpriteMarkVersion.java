package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
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
//?}
