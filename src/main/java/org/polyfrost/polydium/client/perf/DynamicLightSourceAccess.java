package org.polyfrost.polydium.client.perf;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class DynamicLightSourceAccess {
    private static final MethodHandle X, Y, Z, LUMINANCE;

    static {
        try {
            Class<?> source = Class.forName("dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSource");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodType toDouble = MethodType.methodType(double.class, Object.class);
            X = lookup.findVirtual(source, "getDynamicLightX", MethodType.methodType(double.class)).asType(toDouble);
            Y = lookup.findVirtual(source, "getDynamicLightY", MethodType.methodType(double.class)).asType(toDouble);
            Z = lookup.findVirtual(source, "getDynamicLightZ", MethodType.methodType(double.class)).asType(toDouble);
            LUMINANCE = lookup.findVirtual(source, "getLuminance", MethodType.methodType(int.class)).asType(MethodType.methodType(int.class, Object.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DynamicLightSourceAccess() {
    }

    public static double x(Object source) {
        try {
            return (double) X.invokeExact(source);
        } catch (Throwable t) {
            throw sneaky(t);
        }
    }

    public static double y(Object source) {
        try {
            return (double) Y.invokeExact(source);
        } catch (Throwable t) {
            throw sneaky(t);
        }
    }

    public static double z(Object source) {
        try {
            return (double) Z.invokeExact(source);
        } catch (Throwable t) {
            throw sneaky(t);
        }
    }

    public static int luminance(Object source) {
        try {
            return (int) LUMINANCE.invokeExact(source);
        } catch (Throwable t) {
            throw sneaky(t);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneaky(Throwable t) throws T {
        throw (T) t;
    }
}
