package org.polyfrost.polydium.compat;

import org.polyfrost.polydium.client.PolydiumToggles;

public final class XaeroMinimapHudCache {
    private static final long FRAME_NANOS = 1_000_000_000L / 60;

    private long nextRender;
    private int lastKey;

    public boolean reuse(int key) {
        if (!PolydiumToggles.xaeroMinimapHudCache) return false;
        long now = System.nanoTime();
        if (key == lastKey && now - nextRender < 0) return true;
        nextRender = key == lastKey && now - nextRender < FRAME_NANOS ? nextRender + FRAME_NANOS : now + FRAME_NANOS;
        lastKey = key;
        return false;
    }
}
