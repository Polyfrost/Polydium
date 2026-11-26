package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
public final class FramePath {

    private static final int WINDOW_FRAMES = 128;
    private static final double WAIT_LIMIT = 0.10;
    private static final double BLOCKED_LIMIT = 0.20;
    private static final int PROBE_INTERVAL_WINDOWS = 8;
    private static final double PROBE_MARGIN = 0.97;

    private static boolean worker;
    private static boolean frameUsesWorker;
    private static boolean frameEligible;
    private static long lastSubmit;
    private static long windowFrameNanos;
    private static long windowWaitNanos;
    private static long windowAcquireNanos;
    private static int windowFrames;
    private static boolean probed;
    private static boolean probing;
    private static int windowsSinceProbe;
    private static long probeBaselineNanos;

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

    public static void frameSubmitted(long now) {
        if (lastSubmit == 0L) {
            lastSubmit = now;

            return;
        }

        long frameNanos = now - lastSubmit;

        lastSubmit = now;

        if (!frameEligible) {
            worker = false;
            resetProbe();
            resetWindow();

            return;
        }

        windowFrameNanos += frameNanos;
        windowFrames++;

        if (windowFrames < WINDOW_FRAMES)
            return;

        decide(windowFrameNanos / windowFrames);
        resetWindow();
    }

    private static void decide(long frameNanos) {
        if (probing) {
            probing = false;

            if (frameNanos >= probeBaselineNanos * PROBE_MARGIN)
                worker = !worker;

            return;
        }

        if (!probed) {
            if (worker) {
                worker = windowAcquireNanos + windowWaitNanos <= windowFrameNanos * BLOCKED_LIMIT;

                return;
            }

            if (windowWaitNanos < windowFrameNanos * WAIT_LIMIT) {
                worker = true;

                return;
            }
        }

        if (++windowsSinceProbe < PROBE_INTERVAL_WINDOWS)
            return;

        probed = true;
        probing = true;
        windowsSinceProbe = 0;
        probeBaselineNanos = frameNanos;
        worker = !worker;
    }

    public static void reset() {
        worker = false;
        lastSubmit = 0L;
        resetProbe();
        resetWindow();
    }

    private static void resetProbe() {
        probed = false;
        probing = false;
        windowsSinceProbe = 0;
    }

    private static void resetWindow() {
        windowFrameNanos = 0L;
        windowWaitNanos = 0L;
        windowAcquireNanos = 0L;
        windowFrames = 0;
    }
}
//?}
