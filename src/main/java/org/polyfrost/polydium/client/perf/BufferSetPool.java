package org.polyfrost.polydium.client.perf;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;

import java.util.function.Function;

public final class BufferSetPool implements Function<Object, Object> {
    public static final int MAX_POOLED = 256;

    private final ObjectArrayList<ReferenceLinkedOpenHashSet<Object>> free = new ObjectArrayList<>();

    @Override
    public Object apply(Object key) {
        return this.take();
    }

    public ReferenceLinkedOpenHashSet<Object> take() {
        return this.free.isEmpty() ? new ReferenceLinkedOpenHashSet<>() : this.free.pop();
    }

    @SuppressWarnings("unchecked")
    public void release(Object set) {
        if (!(set instanceof ReferenceLinkedOpenHashSet<?> pooled) || this.free.size() >= MAX_POOLED) {
            return;
        }
        for (int i = 0, n = this.free.size(); i < n; i++) {
            if (this.free.get(i) == pooled) {
                return;
            }
        }
        pooled.clear();
        this.free.push((ReferenceLinkedOpenHashSet<Object>) pooled);
    }

    public int pooled() {
        return this.free.size();
    }
}
