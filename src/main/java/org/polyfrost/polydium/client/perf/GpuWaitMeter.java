package org.polyfrost.polydium.client.perf;

public final class GpuWaitMeter {

    private static final long   WINDOW   = 500_000_000L;
    private static final double DECAY    = 0.95;
    private static final double MIN_WAIT = 0.02;
    private static final double NO_GAIN  = 2.0;
    private static final int    PROBE_AT = 60;

    private static long    windowStart = System.nanoTime();
    private static long    waited;
    private static long    waitStart;
    private static long    culled;
    private static long    lastCullSpan;
    private static int     frames;
    private static int     lastCullFrames;
    private static int     cullWindows;
    private static boolean cullingRan;
    private static boolean cullingTested;
    private static boolean cullingHid;
    private static boolean cullingPays;
    private static boolean warm;
    private static double  share;
    private static double  waitWithoutCulling;
    private static double  cullShare;
    private static double  teardown;

    private GpuWaitMeter() {
    }

    public static void beginWait() {
        waitStart = System.nanoTime();
    }

    public static void endWait() {
        long now = System.nanoTime();

        waited += now - waitStart;

        if (now - windowStart < WINDOW)
            return;

        long span = now - windowStart;

        share = (double) waited / span;

        boolean trial = false;

        if (cullingRan) {
            if (warm) {
                cullShare = (double) culled / span;
                teardown  = 0.0;
            }

            waitWithoutCulling = Math.max(share, waitWithoutCulling * DECAY);

            if (cullingTested && !cullingHid)
                cullShare = Math.max(cullShare, waitWithoutCulling * NO_GAIN);
            else
                trial = !warm;

            warm           = cullingTested;
            cullWindows   ++;
            lastCullSpan   = span;
            lastCullFrames = frames;
        } else {
            waitWithoutCulling = share;
            cullShare         *= DECAY;
            teardown           = Math.max(teardown * DECAY, (double) culled / span);

            if (cullWindows > 0 && !cullingPays && lastCullFrames > 0 && frames > 0
                && (double) lastCullSpan / lastCullFrames >= (double) span / frames)
                cullShare = Math.max(cullShare, waitWithoutCulling * NO_GAIN);

            cullWindows = 0;
            warm        = false;
        }

        cullingPays = (trial || waitWithoutCulling > Math.max(cullShare + teardown, MIN_WAIT)) && cullWindows < PROBE_AT;
        windowStart = now;
        waited      = 0L;
        culled        = 0L;
        frames        = 0;
        cullingRan    = false;
        cullingTested = false;
        cullingHid    = false;
    }

    public static void addCullFrame(long nanos, boolean tested, boolean hiding) {
        culled        += nanos;
        cullingRan     = true;
        cullingTested |= tested;
        cullingHid    |= hiding;
    }

    public static void frame() {
        frames ++;
    }

    public static void addCullCost(long nanos) {
        culled += nanos;
    }

    public static boolean cullingPays() {
        return cullingPays;
    }

    public static double waitShare() {
        return share;
    }
}
