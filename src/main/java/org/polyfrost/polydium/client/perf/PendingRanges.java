package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.Random;

public final class PendingRanges {
    private final LongArrayList ranges = new LongArrayList();
    private final long[] scratch = new long[3];

    public void clear() {
        this.ranges.clear();
    }

    public boolean overlaps(long buffer, long start, long end) {
        int before = this.firstAtOrAfter(buffer, end) - 1;
        return before >= 0 && this.ranges.getLong(before * 3) == buffer && start < this.ranges.getLong(before * 3 + 2);
    }

    public void add(long buffer, long start, long end) {
        long[] range = this.scratch;
        range[0] = buffer;
        range[1] = start;
        range[2] = end;
        this.ranges.addElements(this.insertion(buffer, start, end) * 3, range, 0, 3);
    }

    public void add(long buffer, long start, long end, long readBuffer, long readStart, long readEnd) {
        if (readBuffer == 0L) {
            this.add(buffer, start, end);
        } else if (readBuffer == buffer && readStart < end && start < readEnd) {
            this.add(buffer, Math.min(start, readStart), Math.max(end, readEnd));
        } else {
            this.add(buffer, start, end);
            this.add(readBuffer, readStart, readEnd);
        }
    }

    private int insertion(long buffer, long start, long end) {
        LongArrayList ranges = this.ranges;
        int low = 0;
        int high = ranges.size() / 3;
        while (low < high) {
            int middle = low + high >>> 1;
            long middleBuffer = ranges.getLong(middle * 3);
            long middleStart = ranges.getLong(middle * 3 + 1);
            if (middleBuffer < buffer || middleBuffer == buffer && (middleStart < start || middleStart == start && ranges.getLong(middle * 3 + 2) <= end)) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private int firstAtOrAfter(long buffer, long start) {
        LongArrayList ranges = this.ranges;
        int low = 0;
        int high = ranges.size() / 3;
        while (low < high) {
            int middle = low + high >>> 1;
            long middleBuffer = ranges.getLong(middle * 3);
            if (middleBuffer < buffer || middleBuffer == buffer && ranges.getLong(middle * 3 + 1) < start) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    public static void main(String[] args) {
        Random random = new Random(args.length > 0 ? Long.parseLong(args[0]) : 1L);
        long checks = 0;
        for (int sequence = 0; sequence < 50_000; sequence++) {
            PendingRanges sorted = new PendingRanges();
            LongArrayList linear = new LongArrayList();
            for (int op = 0, ops = 1 + random.nextInt(48); op < ops; op++) {
                long[] target = randomRange(random);
                long[] source = random.nextBoolean() ? randomRange(random) : null;
                for (int query = 0; query < 4; query++) {
                    long[] probe = randomRange(random);
                    check(sorted.overlaps(probe[0], probe[1], probe[2]) == linearOverlaps(linear, probe), "overlap differs from the linear scan");
                    checks++;
                }
                boolean flush = linearOverlaps(linear, target) || source != null && linearOverlaps(linear, source);
                check(flush == (sorted.overlaps(target[0], target[1], target[2]) || source != null && sorted.overlaps(source[0], source[1], source[2])), "flush decision differs");
                if (flush) {
                    sorted.clear();
                    linear.clear();
                }
                linear.addElements(linear.size(), target);
                if (source != null) {
                    linear.addElements(linear.size(), source);
                }
                sorted.add(target[0], target[1], target[2], source == null ? 0L : source[0], source == null ? 0L : source[1], source == null ? 0L : source[2]);
            }
        }
        System.out.println("PendingRanges: " + checks + " overlap checks match the linear scan");
    }

    private static long[] randomRange(Random random) {
        long start = random.nextInt(64);
        return new long[]{1 + random.nextInt(3), start, start + random.nextInt(17)};
    }

    private static boolean linearOverlaps(LongArrayList ranges, long[] probe) {
        for (int index = 0; index < ranges.size(); index += 3) {
            if (ranges.getLong(index) == probe[0] && ranges.getLong(index + 1) < probe[2] && probe[1] < ranges.getLong(index + 2)) {
                return true;
            }
        }
        return false;
    }

    private static void check(boolean ok, String what) {
        if (!ok) {
            throw new AssertionError(what);
        }
    }
}
//?}
