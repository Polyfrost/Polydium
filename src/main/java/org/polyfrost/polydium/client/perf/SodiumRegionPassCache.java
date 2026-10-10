package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import java.util.Arrays;
import java.util.Map;

public final class SodiumRegionPassCache {
    public static final int PASSES = 3;

    private Map<?, ?> mapA;
    private Map<?, ?> mapB;
    private final Object[] slotsA = new Object[PASSES];
    private final Object[] slotsB = new Object[PASSES];

    public Object[] slotsFor(Map<?, ?> map) {
        if (map == this.mapA) {
            return this.slotsA;
        }
        if (map == this.mapB) {
            return this.slotsB;
        }
        if (this.mapA == null) {
            this.mapA = map;
            return this.slotsA;
        }
        if (this.mapB == null) {
            this.mapB = map;
            return this.slotsB;
        }
        this.clear();
        this.mapA = map;
        return this.slotsA;
    }

    public void clear() {
        this.mapA = null;
        this.mapB = null;
        Arrays.fill(this.slotsA, null);
        Arrays.fill(this.slotsB, null);
    }
}
//?}
