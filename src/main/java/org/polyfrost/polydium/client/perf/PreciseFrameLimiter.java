package org.polyfrost.polydium.client.perf;

//? if > 1.8.9 && < 26.1 {
/*import org.lwjgl.glfw.GLFW;

import java.util.concurrent.locks.LockSupport;

public final class PreciseFrameLimiter {
    private static final long SPIN_NANOS = 500_000L;
    private static long lastFrame = System.nanoTime();

    private PreciseFrameLimiter() {
    }

    public static void limit(int fps) {
        long due = due(lastFrame, System.nanoTime(), 1_000_000_000L / fps);
        long remaining;
        while ((remaining = due - System.nanoTime()) > 0L) {
            if (remaining > SPIN_NANOS) {
                LockSupport.parkNanos(remaining - SPIN_NANOS);
                GLFW.glfwPollEvents();
            } else {
                Thread.onSpinWait();
            }
        }
        lastFrame = due;
    }

    static long due(long last, long now, long interval) {
        long due = last + interval;
        return now - due > interval ? now : due;
    }

    public static void main(String[] args) {
        check(due(0L, 400L, 1000L) == 1000L, "early frame waits for the schedule");
        check(due(0L, 1500L, 1000L) == 1000L, "late frame does not wait and keeps the schedule");
        check(due(0L, 2000L, 1000L) == 1000L, "exactly one interval behind keeps the schedule");
        check(due(0L, 2001L, 1000L) == 2001L, "more than one interval behind restarts");
    }

    private static void check(boolean ok, String what) {
        if (!ok) {
            throw new AssertionError(what);
        }
    }
}
*///?}
