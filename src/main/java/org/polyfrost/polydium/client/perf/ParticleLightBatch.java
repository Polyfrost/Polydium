package org.polyfrost.polydium.client.perf;

//? if >= 26.1 {
import net.minecraft.client.particle.Particle;
import org.polyfrost.polydium.mixin.client.perf.lambdynlights.ParticleLightSourceInvoker;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public final class ParticleLightBatch {
    private static final int SLOTS = 256;
    private static final int TICK_POOL_SLOTS = 128;

    @SuppressWarnings("unchecked")
    private static final ArrayList<Particle>[] slots = new ArrayList[SLOTS];

    static {
        for (int i = 0; i < SLOTS; i++)
            slots[i] = new ArrayList<>();
    }

    private static volatile boolean pending;

    private ParticleLightBatch() {
    }

    public static boolean defer(Particle particle) {
        int index;
        Thread thread = Thread.currentThread();
        if (thread instanceof ForkJoinWorkerThread worker && worker.getPool() == ForkJoinPool.commonPool())
            index = worker.getPoolIndex();
        else if (thread instanceof ForkJoinWorkerThread worker && ParticleTickOverlap.isTickPool(worker.getPool()))
            index = TICK_POOL_SLOTS + worker.getPoolIndex();
        else
            return false;

        if (index < 0 || index >= SLOTS)
            return false;

        slots[index].add(particle);
        pending = true;
        return true;
    }

    public static void drain() {
        if (!pending)
            return;

        pending = false;

        for (ArrayList<Particle> slot : slots) {
            int size = slot.size();

            if (size == 0)
                continue;

            for (int i = 0; i < size; i++)
                ParticleLightSourceInvoker.polydium$doTickParticle(slot.get(i));

            slot.clear();
        }
    }
}
//?}
