package org.polyfrost.polydium.client.perf;

import java.util.ArrayList;
import java.util.Arrays;

public final class QuadSort {
    private static final int RADIX_THRESHOLD = 80;

    private static final ArrayList<int[]> PREVIOUS = new ArrayList<>();
    private static long frame = -1L;
    private static int next;
    private static int slot;
    private static int[] sharedKeys = new int[0];
    private static final int MAX_SCRATCH = 4096;
    private static final ThreadLocal<long[]> SCRATCH = ThreadLocal.withInitial(() -> new long[64]);

    private QuadSort() {
    }

    public static void sortSmall(int[] perm, int[] keys) {
        int length = perm.length;
        long[] packed = scratch(length);
        for (int i = 0; i < length; i++) {
            int index = perm[i];
            packed[i] = (long) keys[index] << 32 | index & 0xFFFFFFFFL;
        }
        Arrays.sort(packed, 0, length);
        for (int i = 0; i < length; i++) {
            perm[i] = (int) packed[i];
        }
    }

    private static long[] scratch(int length) {
        if (length > MAX_SCRATCH) {
            return new long[length];
        }
        long[] packed = SCRATCH.get();
        if (packed.length < length) {
            packed = new long[Math.min(MAX_SCRATCH, Math.max(length, packed.length << 1))];
            SCRATCH.set(packed);
        }
        return packed;
    }

    public static boolean reuse(long currentFrame, int[] perm, int[] keys) {
        int length = perm.length;
        int[] order = keys.length == length ? reuseShared(currentFrame, keys, length) : advance(currentFrame);
        if (order == null) {
            return false;
        }
        System.arraycopy(order, 0, perm, 0, length);
        return true;
    }

    public static int[] keys(int length) {
        if (sharedKeys.length < length) {
            sharedKeys = new int[length];
        }
        return sharedKeys;
    }

    public static int[] reuseShared(long currentFrame, int[] keys, int length) {
        advance(currentFrame);
        if (slot >= PREVIOUS.size()) {
            return null;
        }
        int[] order = PREVIOUS.get(slot);
        if (order.length != length) {
            return null;
        }
        boolean signed = length <= RADIX_THRESHOLD;
        for (int i = 1; i < length; i++) {
            int before = order[i - 1];
            int after = order[i];
            int a = keys[before];
            int b = keys[after];
            int comparison = signed ? Integer.compare(a, b) : Integer.compareUnsigned(a, b);
            if (comparison > 0 || comparison == 0 && before > after) {
                return null;
            }
        }
        return order;
    }

    private static int[] advance(long currentFrame) {
        if (currentFrame != frame) {
            frame = currentFrame;
            next = 0;
        }
        slot = next++;
        return null;
    }

    public static void rememberOwned(int[] perm) {
        if (slot < PREVIOUS.size()) {
            PREVIOUS.set(slot, perm);
        } else if (slot == PREVIOUS.size()) {
            PREVIOUS.add(perm);
        }
    }

    public static void remember(int[] perm) {
        if (slot < PREVIOUS.size()) {
            int[] order = PREVIOUS.get(slot);
            if (order.length == perm.length) {
                System.arraycopy(perm, 0, order, 0, perm.length);
            } else {
                PREVIOUS.set(slot, perm.clone());
            }
        } else if (slot == PREVIOUS.size()) {
            PREVIOUS.add(perm.clone());
        }
    }
}
