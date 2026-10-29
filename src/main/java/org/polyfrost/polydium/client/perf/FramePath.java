package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
public final class FramePath {

    private static final int    WINDOW_FRAMES = 128;
    private static final double WAIT_LIMIT    = 0.10;
    private static final double BLOCKED_LIMIT = 0.20;

    private static boolean worker;
    private static boolean frameUsesWorker;
    private static boolean frameEligible;
    private static long    lastSubmit;
    private static long    windowFrameNanos;
    private static long    windowWaitNanos;
    private static long    windowAcquireNanos;
    private static int     windowFrames;

    private FramePath() {
    }

    public static boolean beginFrame(boolean eligible) {
        frameEligible   = eligible;
        frameUsesWorker = eligible && worker;

        return frameUsesWorker;
    }

    public static boolean nextFrameAcquiresEarly(boolean eligible) {
        return eligible && !worker;
    }

    public static boolean frameUsesWorker() {
        return frameUsesWorker;
    }

    public static void recordGpuWait(long nanos) {
        windowWaitNanos += nanos;
    }

    public static void recordAcquire(long nanos) {
        windowAcquireNanos += nanos;
    }

    public static void frameSubmitted() {
        long now = System.nanoTime();

        if (lastSubmit == 0L) {
            lastSubmit = now;

            return;
        }

        long frameNanos = now - lastSubmit;

        lastSubmit = now;

        if (!frameEligible) {
            worker = false;
            resetWindow();

            return;
        }

        windowFrameNanos += frameNanos;
        windowFrames ++;

        if (windowFrames < WINDOW_FRAMES)
            return;

        if (worker)
            worker = windowAcquireNanos + windowWaitNanos <= windowFrameNanos * BLOCKED_LIMIT;
        else
            worker = windowWaitNanos < windowFrameNanos * WAIT_LIMIT;

        resetWindow();
    }

    public static void reset() {
        worker     = false;
        lastSubmit = 0L;
        resetWindow();
    }

    private static void resetWindow() {
        windowFrameNanos   = 0L;
        windowWaitNanos    = 0L;
        windowAcquireNanos = 0L;
        windowFrames       = 0;
    }
}
//?}
