package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import org.lwjgl.system.MemoryStack;

public final class RenderThreadStack {
    private static Thread thread;
    private static MemoryStack stack;

    private RenderThreadStack() {
    }

    public static void capture(MemoryStack captured) {
        if (thread == null) {
            stack = captured;
            thread = Thread.currentThread();
        }
    }

    public static boolean isOwner() {
        return Thread.currentThread() == thread;
    }

    public static MemoryStack stack() {
        return stack;
    }
}
//?}
