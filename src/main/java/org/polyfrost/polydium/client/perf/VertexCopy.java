package org.polyfrost.polydium.client.perf;

import org.lwjgl.system.MemoryUtil;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public final class VertexCopy {

    public interface Owner {
        void polydium$finishVertexCopy();
    }

    private static final int  CHUNK_BYTES      = 32768;
    private static final int  IDLE             = 0;
    private static final int  QUEUED           = 1;
    private static final int  RUNNING          = 2;
    private static final long FAST_FRAME_NANOS = 250_000L;
    private static final int  SMOOTHING_SHIFT  = 4;

    private static boolean    levelPrepare;
    private static Owner      inFlight;
    private static VertexCopy toStart;
    private static long       lastLevelFrame;
    private static long       averageFrameNanos;
    private static boolean    fastFrames;

    private final AtomicInteger next  = new AtomicInteger();
    private final AtomicInteger state = new AtomicInteger();

    private volatile Throwable failure;
    private long[]             sources = new long[64];
    private long[]             targets = new long[64];
    private int[]              sizes   = new int[64];
    private int                count;
    private boolean            pending;

    public static void beginLevelPrepare() {
        long now = System.nanoTime();

        if (lastLevelFrame != 0L) {
            long frameNanos = now - lastLevelFrame;

            averageFrameNanos = averageFrameNanos == 0L ? frameNanos : averageFrameNanos + (frameNanos - averageFrameNanos >> SMOOTHING_SHIFT);
            fastFrames        = averageFrameNanos < FAST_FRAME_NANOS;
        }

        lastLevelFrame = now;
        levelPrepare   = true;
    }

    public static void endLevelPrepare() {
        levelPrepare = false;

        VertexCopy copy = toStart;

        toStart = null;

        if (copy != null)
            copy.startInBackground();
    }

    public static boolean isLevelPrepare() {
        return levelPrepare;
    }

    public static void track(Owner owner) {
        if (inFlight != null && inFlight != owner)
            inFlight.polydium$finishVertexCopy();

        inFlight = owner;
    }

    public static void untrack(Owner owner) {
        if (inFlight == owner)
            inFlight = null;
    }

    public static void finishInFlight() {
        if (inFlight != null)
            inFlight.polydium$finishVertexCopy();
    }

    public void begin() {
        if (this.pending)
            throw new IllegalStateException("The previous vertex copies have not finished");

        this.count = 0;
    }

    public void add(long source, long target, int size) {
        for (int offset = 0; offset < size; offset += CHUNK_BYTES) {
            if (this.count == this.sizes.length) {
                this.sources = Arrays.copyOf(this.sources, this.count * 2);
                this.targets = Arrays.copyOf(this.targets, this.count * 2);
                this.sizes   = Arrays.copyOf(this.sizes, this.count * 2);
            }

            this.sources[this.count] = source + offset;
            this.targets[this.count] = target + offset;
            this.sizes[this.count]   = Math.min(CHUNK_BYTES, size - offset);
            this.count ++;
        }
    }

    public void seal() {
        if (this.pending)
            throw new IllegalStateException("The previous vertex copies have not finished");

        if (this.count == 0)
            return;

        this.next.set(0);
        this.pending = true;
        toStart      = this;
    }

    private void startInBackground() {
        if (!this.pending)
            return;

        if (this.state.get() != IDLE)
            throw new IllegalStateException("The vertex copies were already started");

        this.state.set(QUEUED);
        Worker.queue(this, !fastFrames);
    }

    public void finish() {
        if (!this.pending)
            return;

        try {
            this.state.compareAndSet(QUEUED, IDLE);
            this.copyClaimed();
        } finally {
            while (this.state.get() == RUNNING)
                Thread.onSpinWait();

            this.pending = false;

            if (toStart == this)
                toStart = null;
        }

        Throwable thrown = this.failure;

        if (thrown == null)
            return;

        this.failure = null;

        throw new IllegalStateException("Copying vertices on the vertex copy thread failed", thrown);
    }

    private void runQueued() {
        if (!this.state.compareAndSet(QUEUED, RUNNING))
            return;

        try {
            this.copyClaimed();
        } catch (Throwable thrown) {
            this.failure = thrown;
        } finally {
            this.state.set(IDLE);
        }
    }

    private void copyClaimed() {
        int jobs = this.count;

        for (int job = this.next.getAndIncrement(); job < jobs; job = this.next.getAndIncrement())
            MemoryUtil.memCopy(this.sources[job], this.targets[job], this.sizes[job]);
    }

    private static final class Worker {

        private static final AtomicReference<VertexCopy> JOB    = new AtomicReference<>();
        private static final Thread                      THREAD = Thread.ofPlatform().name("Polydium Vertex Copy").daemon().priority(Thread.MAX_PRIORITY).start(Worker::work);

        private static void queue(VertexCopy copy, boolean wake) {
            JOB.set(copy);

            if (wake)
                LockSupport.unpark(THREAD);
        }

        private static void work() {
            while (true) {
                if (JOB.get() == null) {
                    LockSupport.park(Worker.class);
                    continue;
                }

                VertexCopy copy = JOB.getAndSet(null);

                if (copy != null)
                    copy.runQueued();
            }
        }
    }
}
