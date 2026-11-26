package org.polyfrost.polydium.client.perf;

import java.util.IdentityHashMap;
import java.util.function.Predicate;

public final class OitlessTextCache<T> {
    private final int capacity;
    private final IdentityHashMap<T, Integer> answers = new IdentityHashMap<>();
    private int generation = FontGeneration.get();

    public OitlessTextCache(int capacity) {
        this.capacity = capacity;
    }

    public boolean lacksOit(T text, int variant, Predicate<T> scan) {
        int current = FontGeneration.get();
        if (current != this.generation) {
            this.generation = current;
            this.answers.clear();
        }
        Integer bits = this.answers.get(text);
        int known = bits == null ? 0 : bits;
        int computed = 1 << (variant * 2);
        if ((known & computed) != 0)
            return (known & computed << 1) != 0;
        boolean lacking = scan.test(text);
        if (bits == null && this.answers.size() >= this.capacity)
            this.answers.clear();
        this.answers.put(text, known | computed | (lacking ? computed << 1 : 0));
        return lacking;
    }

    public int size() {
        return this.answers.size();
    }
}
