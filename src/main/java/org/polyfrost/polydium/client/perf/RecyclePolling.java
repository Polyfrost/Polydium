package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
public final class RecyclePolling {
    private static long pendingFrame = -1L;
    private static long lowestPending;

    private RecyclePolling() {
    }

    public static boolean isKnownPending(long submitIndex) {
        return pendingFrame == PerfClock.frame && submitIndex >= lowestPending;
    }

    public static void markPending(long submitIndex) {
        if (pendingFrame != PerfClock.frame) {
            pendingFrame = PerfClock.frame;
            lowestPending = submitIndex;
        } else if (submitIndex < lowestPending) {
            lowestPending = submitIndex;
        }
    }
}
//?}
