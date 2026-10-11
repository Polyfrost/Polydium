package org.polyfrost.polydium.client.perf;

import java.util.Arrays;

public final class FrameStats {
    private static final long SMOOTH_NANOS = 500_000_000L;
    private static final long WINDOW_NANOS = 5_000_000_000L;
    private static final long UPDATE_NANOS = 500_000_000L;
    private static final int CAPACITY = 1 << 14;
    private static final int MASK = CAPACITY - 1;

    private final long[] timestamps = new long[CAPACITY];
    private final long[] deltas = new long[CAPACITY];
    private final long[] sorted = new long[CAPACITY];
    private int head;
    private int size;
    private long lastFrame = -1;
    private long lastUpdate;

    public double smooth;
    public double average;
    public double onePercentLow;
    public double pointOnePercentLow;

    public void pause() {
        this.lastFrame = -1;
    }

    public boolean frame(long now) {
        if (this.lastFrame != -1) {
            if (this.size == CAPACITY) {
                this.head = (this.head + 1) & MASK;
                this.size--;
            }

            int index = (this.head + this.size++) & MASK;
            this.timestamps[index] = now;
            this.deltas[index] = now - this.lastFrame;
        }

        this.lastFrame = now;

        while (this.size > 0 && now - this.timestamps[this.head] > WINDOW_NANOS) {
            this.head = (this.head + 1) & MASK;
            this.size--;
        }

        if (now - this.lastUpdate < UPDATE_NANOS)
            return false;

        this.lastUpdate = now;
        if (this.size == 0) {
            this.smooth = this.average = this.onePercentLow = this.pointOnePercentLow = 0;
            return true;
        }

        long total = 0;
        long recentTotal = 0;
        int recent = 0;
        for (int i = 0; i < this.size; i++) {
            int index = (this.head + i) & MASK;
            long delta = this.deltas[index];
            this.sorted[i] = delta;
            total += delta;
            if (now - this.timestamps[index] <= SMOOTH_NANOS) {
                recentTotal += delta;
                recent++;
            }
        }

        Arrays.sort(this.sorted, 0, this.size);
        this.average = this.size * 1_000_000_000.0 / total;
        this.onePercentLow = this.low(1.0);
        this.pointOnePercentLow = this.low(0.1);
        this.smooth = recent == 0 ? 0 : recent * 1_000_000_000.0 / recentTotal;
        return true;
    }

    private double low(double percent) {
        int count = Math.max(1, (int) Math.ceil(this.size * (percent / 100.0)));
        long sum = 0;
        for (int i = this.size - count; i < this.size; i++)
            sum += this.sorted[i];

        return count * 1_000_000_000.0 / sum;
    }
}
