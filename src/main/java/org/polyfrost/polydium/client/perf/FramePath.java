package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
public final class FramePath {

    private static final int WINDOW_FRAMES = 128;
    private static final int PROBE_WINDOWS = 16;
    private static final int MAX_PROBE_WINDOWS = PROBE_WINDOWS << 3;
    private static final double BLOCKED_LIMIT = 0.20;

    private static boolean worker;
    private static boolean frameUsesWorker;
    private static boolean frameEligible;
    private static long lastSubmit;
    private static long windowFrameNanos;
    private static long windowWaitNanos;
    private static long windowAcquireNanos;
    private static int windowFrames;
    private static long workerFrameNanos;
    private static long inlineFrameNanos;
    private static int workerAge = Integer.MAX_VALUE / 2;
    private static int inlineAge = Integer.MAX_VALUE / 2;
    private static int probeWindows = PROBE_WINDOWS;

    private FramePath() {
    }

    public static boolean beginFrame(boolean eligible) {
        frameEligible = eligible;
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
        frameSubmitted(System.nanoTime());
    }

    static void frameSubmitted(long now) {
        if (lastSubmit == 0L) {
            lastSubmit = now;

            return;
        }

        long frameNanos = now - lastSubmit;

        lastSubmit = now;

        if (!frameEligible) {
            reset(now);

            return;
        }

        windowFrameNanos += frameNanos;
        windowFrames++;

        if (windowFrames < WINDOW_FRAMES)
            return;

        long average = windowFrameNanos / windowFrames;

        workerAge++;
        inlineAge++;

        if (worker) {
            workerFrameNanos = average;
            workerAge = 0;
            if (inlineAge <= PROBE_WINDOWS) {
                worker = average < inlineFrameNanos;
                probeWindows = worker ? PROBE_WINDOWS : Math.min(probeWindows * 2, MAX_PROBE_WINDOWS);
            } else {
                worker = windowAcquireNanos + windowWaitNanos <= windowFrameNanos * BLOCKED_LIMIT;
            }
        } else {
            inlineFrameNanos = average;
            inlineAge = 0;
            worker = workerAge > probeWindows || workerFrameNanos < average;
        }

        resetWindow();
    }

    public static void reset() {
        reset(0L);
    }

    private static void reset(long now) {
        worker = false;
        lastSubmit = now;
        workerAge = Integer.MAX_VALUE / 2;
        inlineAge = Integer.MAX_VALUE / 2;
        probeWindows = PROBE_WINDOWS;
        resetWindow();
    }

    private static void resetWindow() {
        windowFrameNanos = 0L;
        windowWaitNanos = 0L;
        windowAcquireNanos = 0L;
        windowFrames = 0;
    }
}
//?}
