package org.polyfrost.polydium.client.perf;

import java.util.concurrent.ThreadLocalRandom;

public final class ParticleReservoir {
    private static final int MAX = 16384;
    private static final int SOFT = 12288;

    private ParticleReservoir() {
    }

    public static boolean accepts(int size) {
        if (size >= MAX) {
            return false;
        }
        if (size < SOFT) {
            return true;
        }
        float free = (MAX - size) / (float) (MAX - SOFT);
        return ThreadLocalRandom.current().nextFloat() < free * free;
    }
}
