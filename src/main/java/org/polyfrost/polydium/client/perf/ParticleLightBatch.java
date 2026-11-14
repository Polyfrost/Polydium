package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.polyfrost.polydium.mixin.client.perf.lambdynlights.ParticleLevelAccessor;
import org.polyfrost.polydium.mixin.client.perf.lambdynlights.ParticleLightSourceInvoker;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ParticleLightBatch {
    private static final int SLOTS = 128;

    @SuppressWarnings("unchecked")
    private static final ArrayList<Particle>[] slots = new ArrayList[SLOTS];

    static {
        for (int i = 0; i < SLOTS; i++)
            slots[i] = new ArrayList<>();
    }

    private static final ConcurrentLinkedQueue<Particle> overflow = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean overflowScheduled = new AtomicBoolean();

    private static volatile boolean pending;

    private ParticleLightBatch() {
    }

    public static boolean defer(Particle particle) {
        if (Thread.currentThread() instanceof ForkJoinWorkerThread worker && worker.getPoolIndex() < SLOTS && ParticleTickOverlap.isTickPool(worker.getPool())) {
            slots[worker.getPoolIndex()].add(particle);
            pending = true;
            return true;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.isSameThread())
            return false;

        overflow.add(particle);
        pending = true;
        if (!overflowScheduled.getAndSet(true))
            mc.execute(ParticleLightBatch::drainOverflow);
        return true;
    }

    public static void drain() {
        if (!pending)
            return;

        pending = false;
        ClientLevel level = Minecraft.getInstance().level;

        for (ArrayList<Particle> slot : slots) {
            int size = slot.size();

            if (size == 0)
                continue;

            for (int i = 0; i < size; i++)
                tick(slot.get(i), level);

            slot.clear();
        }

        drainOverflow();
    }

    public static void drainOverflow() {
        overflowScheduled.set(false);
        ClientLevel level = Minecraft.getInstance().level;
        Particle particle;
        while ((particle = overflow.poll()) != null)
            tick(particle, level);
    }

    private static void tick(Particle particle, ClientLevel level) {
        if (((ParticleLevelAccessor) particle).polydium$level() == level)
            ParticleLightSourceInvoker.polydium$doTickParticle(particle);
    }
}
//?}
