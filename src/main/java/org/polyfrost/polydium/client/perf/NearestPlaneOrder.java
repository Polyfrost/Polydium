package org.polyfrost.polydium.client.perf;

import java.util.Arrays;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class NearestPlaneOrder {
    public static final int STRIDE = 20;
    private static final int VERTICES_PER_QUAD = 4;
    private static final int QUAD_SIZE = STRIDE * VERTICES_PER_QUAD;
    private static final int AXES = 3;
    private static final int STEPS_PER_PLANE = 32;

    private static final ThreadLocal<long[][]> ORDER = ThreadLocal.withInitial(() -> new long[1][0]);
    private static final ThreadLocal<int[][]> DESTINATIONS = ThreadLocal.withInitial(() -> new int[1][0]);

    private NearestPlaneOrder() {
    }

    public static void sort(long base, int quads, int facing) {
        if (quads < 2)
            return;

        long[][] orderHolder = ORDER.get();
        int[][] destinationHolder = DESTINATIONS.get();

        if (orderHolder[0].length < quads) {
            orderHolder[0] = new long[quads];
            destinationHolder[0] = new int[quads];
        }

        long[] order = orderHolder[0];
        int[] destinations = destinationHolder[0];
        int axis = facing % AXES;
        boolean positive = facing < AXES;
        boolean sorted = true;

        for (int quad = 0; quad < quads; quad++) {
            long first = base + (long) quad * QUAD_SIZE;
            int plane = Integer.MAX_VALUE;

            for (int vertex = 0; vertex < VERTICES_PER_QUAD; vertex++) {
                int value = coordinate(first + (long) vertex * STRIDE, axis);
                plane = Math.min(plane, positive ? value : -value);
            }

            order[quad] = (long) -Math.floorDiv(plane + STEPS_PER_PLANE / 2, STEPS_PER_PLANE) << 32 | quad;
            sorted &= quad == 0 || order[quad - 1] < order[quad];
        }

        if (sorted)
            return;

        Arrays.sort(order, 0, quads);

        for (int position = 0; position < quads; position++)
            destinations[(int) order[position]] = position;

        permute(base, QUAD_SIZE, destinations, quads);
    }

    static int coordinate(long vertex, int axis) {
        int shift = axis * 10;
        return (MemoryUtil.memGetInt(vertex) >>> shift & 0x3FF) << 10 | MemoryUtil.memGetInt(vertex + 4) >>> shift & 0x3FF;
    }

    private static void permute(long base, int quadSize, int[] destinations, int count) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long carried = stack.nmalloc(quadSize);
            long displaced = stack.nmalloc(quadSize);

            for (int start = 0; start < count; start++) {
                if (destinations[start] == start)
                    continue;

                MemoryUtil.memCopy(base + (long) start * quadSize, carried, quadSize);

                int current = start;

                do {
                    int target = destinations[current];
                    long address = base + (long) target * quadSize;

                    destinations[current] = current;

                    if (target != start)
                        MemoryUtil.memCopy(address, displaced, quadSize);

                    MemoryUtil.memCopy(carried, address, quadSize);

                    long swap = carried;

                    carried = displaced;
                    displaced = swap;
                    current = target;
                } while (current != start);
            }
        }
    }
}
