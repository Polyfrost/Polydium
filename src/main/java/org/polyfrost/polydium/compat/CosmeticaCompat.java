package org.polyfrost.polydium.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public final class CosmeticaCompat {
    private static final MethodHandle PREPARED_ICON = preparedIcon();

    private CosmeticaCompat() {
    }

    public static boolean iconPending() {
        if (PREPARED_ICON == null) {
            return false;
        }
        try {
            return PREPARED_ICON.invoke() != null;
        } catch (Throwable t) {
            return false;
        }
    }

    private static MethodHandle preparedIcon() {
        if (!Mods.loaded("cosmetica-core")) {
            return null;
        }
        try {
            Field field = Class.forName("cc.cosmetica.core.impl.NametagRenderer", false, CosmeticaCompat.class.getClassLoader()).getDeclaredField("preparedIcon");
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectGetter(field);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
