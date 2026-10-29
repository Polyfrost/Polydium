package org.polyfrost.polydium.compat;

import org.polyfrost.polydium.client.perf.FontGeneration;

public final class FontrastCompat {
    public static final String CONFIG_CLASS = "at.yedel.fontrast.config.FontrastConfig";

    private static volatile boolean forcesObfuscation;

    private FontrastCompat() {
    }

    public static boolean forcesObfuscation() {
        return forcesObfuscation;
    }

    public static void configChanged(Object config) {
        try {
            forcesObfuscation = (boolean) config.getClass().getMethod("isObfuscated", boolean.class).invoke(config, false);
        } catch (ReflectiveOperationException | RuntimeException e) {
            forcesObfuscation = false;
        }
        FontGeneration.bump();
    }
}
