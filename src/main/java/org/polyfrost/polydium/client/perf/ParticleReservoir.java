package org.polyfrost.polydium.client.perf;

import com.google.common.collect.EvictingQueue;

import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public final class ParticleReservoir {
    private ParticleReservoir() {
    }

    public static boolean accepts(Queue<?> particles) {
        if (!(particles instanceof EvictingQueue<?> bounded)) {
            return true;
        }
        int free = bounded.remainingCapacity();
        if (free == 0) {
            return false;
        }
        int band = (particles.size() + free) / 4;
        if (free >= band) {
            return true;
        }
        float fraction = free / (float) band;
        return ThreadLocalRandom.current().nextFloat() < fraction * fraction;
    }
}
