package org.polyfrost.polydium.compat;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public final class XaeroRegionScanGate {
    private static final int IDLE_PASS_MASK = 7;

    private ReferenceOpenHashSet<Object> previous = new ReferenceOpenHashSet<>();
    private ReferenceOpenHashSet<Object> pending = new ReferenceOpenHashSet<>();
    private Object current;
    private int pass;

    public boolean scan(Object region) {
        current = region;
        return previous.contains(region) || (pass + System.identityHashCode(region) & IDLE_PASS_MASK) == 0;
    }

    public void markPending() {
        pending.add(current);
    }

    public void endPass() {
        ReferenceOpenHashSet<Object> swap = previous;
        previous = pending;
        pending = swap;
        pending.clear();
        current = null;
        pass++;
    }
}
