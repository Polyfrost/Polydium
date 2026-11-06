package org.polyfrost.polydium.compat;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.polyfrost.polydium.client.PolydiumToggles;

public final class XaeroRadarIconCache {
    private static final long REFRESH_NANOS = 1_000_000_000L;

    private final Int2ObjectOpenHashMap<Object> icons = new Int2ObjectOpenHashMap<>();
    private long refreshAt;

    public Object get(int entityId) {
        if (!PolydiumToggles.xaeroRadarIconCache) return null;
        long now = System.nanoTime();
        if (now - refreshAt >= 0) {
            icons.clear();
            refreshAt = now + REFRESH_NANOS;
        }
        return icons.get(entityId);
    }

    public void put(int entityId, Object icon) {
        if (icon != null && PolydiumToggles.xaeroRadarIconCache) icons.put(entityId, icon);
    }

    public void clear() {
        icons.clear();
    }
}
