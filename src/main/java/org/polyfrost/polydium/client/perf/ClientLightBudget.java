package org.polyfrost.polydium.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

public final class ClientLightBudget {
    private static final long BUDGET_NS = 2_000_000L;
    private static final long MAX_DELAY_NS = 50_000_000L;

    private static long start, deferredSince;
    private static boolean deferred, tickFrame, flushing;
    public static boolean ticked;
    public static boolean flush;

    private ClientLightBudget() {
    }

    public static void begin(boolean client) {
        if (!client) {
            return;
        }
        start = System.nanoTime();
        flushing = flush;
        flush = false;
        tickFrame = ticked && PolydiumToggles.clientLightBudget;
        ticked = false;
        if (!deferred) {
            deferredSince = 0L;
        }
        deferred = false;
    }

    public static boolean takeNext(boolean client) {
        if (!client || flushing || !PolydiumToggles.clientLightBudget) {
            return true;
        }
        long now = System.nanoTime();
        if (!tickFrame && now - start < BUDGET_NS) {
            return true;
        }
        if (deferredSince == 0L) {
            deferredSince = start;
        }
        if (now - deferredSince > MAX_DELAY_NS) {
            return true;
        }
        deferred = true;
        return false;
    }
}
