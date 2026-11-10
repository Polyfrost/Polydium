package org.polyfrost.polydium.compat;

//? if >= 1.21.11 {
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.polyfrost.polydium.client.PolydiumConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

public final class PolyReflexCompat {

    public static final boolean LOADED = Mods.loaded("polyreflex");

    private static final Logger LOGGER = LoggerFactory.getLogger("Polydium");

    private static final MethodHandle FRAME_ID;
    private static final MethodHandle REFLEX;
    private static final MethodHandle ANTI_LAG;
    private static final MethodHandle BEFORE_PRESENT;
    private static final MethodHandle AFTER_PRESENT;

    private static final boolean LINKED;

    private static long presentingFrame;

    static {
        MethodHandle frameId = null, reflex = null, antiLag = null, beforePresent = null, afterPresent = null;

        if (LOADED) {
            try {
                Class<?> lowLatency = Class.forName("org.polyfrost.polyreflex.LowLatency");
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(lowLatency, MethodHandles.lookup());

                frameId       = lookup.findStaticGetter(lowLatency, "frameId", long.class);
                reflex        = lookup.findStatic(lowLatency, "reflex", methodType(boolean.class));
                antiLag       = lookup.findStatic(lowLatency, "antiLag", methodType(boolean.class));
                beforePresent = lookup.findStatic(lowLatency, "beforePresent", methodType(boolean.class, VkQueue.class, VkPresentInfoKHR.class, MemoryStack.class));
                afterPresent  = lookup.findStatic(lowLatency, "afterPresent", methodType(void.class, MemoryStack.class));
            } catch (ReflectiveOperationException exception) {
                LOGGER.warn("PolyReflex internals changed, presenting inline on the main thread", exception);
                frameId = null;
            }
        }

        FRAME_ID       = frameId;
        REFLEX         = reflex;
        ANTI_LAG       = antiLag;
        BEFORE_PRESENT = beforePresent;
        AFTER_PRESENT  = afterPresent;
        LINKED         = frameId != null;
    }

    private PolyReflexCompat() {
    }

    public static boolean presentsInline() {
        return LOADED && !LINKED;
    }

    public static boolean allowsWorker(int framerateLimit) {
        if (!LOADED)
            return true;

        return LINKED && framerateLimit >= PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF && (call(REFLEX) || call(ANTI_LAG));
    }

    /** Called on the main thread when a frame is handed to the present thread, before PolyReflex starts the next one. */
    public static void captureFrame() {
        if (!LINKED)
            return;

        try {
            presentingFrame = (long) FRAME_ID.invokeExact();
        } catch (Throwable thrown) {
            throw new IllegalStateException("Failed to read the PolyReflex frame id", thrown);
        }
    }

    public static long frameId(long frameId, boolean onPresentThread) {
        return onPresentThread ? presentingFrame : frameId;
    }

    public static boolean beforePresent(VkQueue queue, VkPresentInfoKHR presentInfo, MemoryStack stack) {
        if (!LINKED)
            return false;

        try {
            return (boolean) BEFORE_PRESENT.invokeExact(queue, presentInfo, stack);
        } catch (Throwable thrown) {
            throw new IllegalStateException("PolyReflex present hook failed", thrown);
        }
    }

    public static void afterPresent(MemoryStack stack) {
        try {
            AFTER_PRESENT.invokeExact(stack);
        } catch (Throwable thrown) {
            throw new IllegalStateException("PolyReflex present hook failed", thrown);
        }
    }

    private static boolean call(MethodHandle handle) {
        try {
            return (boolean) handle.invokeExact();
        } catch (Throwable thrown) {
            throw new IllegalStateException("PolyReflex mode query failed", thrown);
        }
    }
}
//?}
