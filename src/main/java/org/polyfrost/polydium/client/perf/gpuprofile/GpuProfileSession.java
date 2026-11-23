package org.polyfrost.polydium.client.perf.gpuprofile;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

final class GpuProfileSession {
    final GpuProfiler.Request request;
    final long startNanos;
    final long endNanos;
    final Map<String, String> environment;
    final boolean passLevel;

    int frames;
    int skippedFrames;
    int overflowFrames;
    int recordedFrames;
    int incompletePresentFrames;
    int samples;
    final LongArrayList cpuFrames = new LongArrayList();
    final LongArrayList allCpuFrames = new LongArrayList();
    final LongArrayList gpuSpans = new LongArrayList();
    final LongArrayList measured = new LongArrayList();
    final LongArrayList presentWaits = new LongArrayList();
    final Object2IntLinkedOpenHashMap<String> presentThreads = new Object2IntLinkedOpenHashMap<>();

    final Totals byKey = new Totals();
    final Totals byPass = new Totals();
    final Int2IntOpenHashMap passGroups = new Int2IntOpenHashMap();

    GpuProfileSession(GpuProfiler.Request request, long now, Map<String, String> environment, boolean passLevel) {
        this.request = request;
        this.startNanos = now;
        this.endNanos = now + request.seconds() * 1_000_000_000L;
        this.environment = environment;
        this.passLevel = passLevel;
    }

    void frameEnded(long presentWaitNanos, @Nullable String presentThread, boolean presentMissing) {
        presentWaits.add(presentWaitNanos);

        if (presentMissing)
            incompletePresentFrames++;

        if (presentThread != null)
            presentThreads.addTo(presentThread, 1);
    }

    void mergePasses(int first, int continued) {
        int group = passGroup(first);
        int other = passGroup(continued);

        if (group != other)
            passGroups.put(other, group);
    }

    int passGroup(int pass) {
        while (passGroups.containsKey(pass))
            pass = passGroups.get(pass);

        return pass;
    }

    void addFrame(int[] keys, int[] draws, long[] stamps, int count, long cpuNanos, boolean overflow, List<long[]> keyParts) {
        frames++;
        cpuFrames.add(cpuNanos);

        if (overflow)
            overflowFrames++;

        long sum = 0;
        long previousEnd = count == 0 ? 0L : stamps[0];

        for (int i = 0; i < count; i++) {
            int key = keys[i];

            if (key == GpuProfiler.DISCARDED)
                continue;

            long end = stamps[i * 2 + 1];
            long duration = Math.max(0L, end - (passLevel ? previousEnd : stamps[i * 2]));

            previousEnd = Math.max(previousEnd, end);
            sum += duration;
            samples++;
            byKey.add(key, duration, draws[i]);
            byPass.add((int) keyParts.get(key)[0], duration, draws[i]);
        }

        byKey.endFrame();
        byPass.endFrame();
        measured.add(sum);
        gpuSpans.add(count == 0 ? 0L : Math.max(0L, stamps[count * 2 - 1] - stamps[0]));
    }

    static final class Totals {
        long[] nanos = new long[256];
        long[] maxFrameNanos = new long[256];
        long[] calls = new long[256];
        long[] draws = new long[256];
        int[] framesSeen = new int[256];

        private long[] frameNanos = new long[256];
        private int[] touched = new int[256];
        private boolean[] isTouched = new boolean[256];
        private int touchedCount;

        void add(int id, long duration, int drawCount) {
            grow(id + 1);

            if (!isTouched[id]) {
                isTouched[id] = true;
                touched[touchedCount++] = id;
            }

            frameNanos[id] += duration;

            if (drawCount != GpuProfiler.CONTINUED) {
                calls[id]++;
                draws[id] += drawCount;
            }
        }

        void endFrame() {
            for (int i = 0; i < touchedCount; i++) {
                int id = touched[i];
                long frame = frameNanos[id];

                nanos[id] += frame;
                maxFrameNanos[id] = Math.max(maxFrameNanos[id], frame);
                framesSeen[id]++;
                frameNanos[id] = 0;
                isTouched[id] = false;
            }

            touchedCount = 0;
        }

        int size() {
            return nanos.length;
        }

        private void grow(int needed) {
            if (needed <= nanos.length)
                return;

            int size = Math.max(needed, nanos.length * 2);

            nanos = Arrays.copyOf(nanos, size);
            maxFrameNanos = Arrays.copyOf(maxFrameNanos, size);
            calls = Arrays.copyOf(calls, size);
            draws = Arrays.copyOf(draws, size);
            framesSeen = Arrays.copyOf(framesSeen, size);
            frameNanos = Arrays.copyOf(frameNanos, size);
            isTouched = Arrays.copyOf(isTouched, size);
            touched = Arrays.copyOf(touched, size);
        }
    }
}
