package org.polyfrost.polydium.client.perf;

import com.google.common.collect.EvictingQueue;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public final class CyclingParticleIterator<P> implements Iterator<P> {
    private final Queue<P> queue;
    private int remaining;
    private P current;

    private CyclingParticleIterator(Queue<P> queue) {
        this.queue = queue;
        this.remaining = queue.size();
    }

    @SuppressWarnings("unchecked")
    public static <P> Iterator<P> of(Iterable<P> particles) {
        return particles instanceof ArrayDeque<?> || particles instanceof EvictingQueue<?> ? new CyclingParticleIterator<>((Queue<P>) particles) : null;
    }

    @Override
    public boolean hasNext() {
        this.requeue();
        return this.remaining > 0;
    }

    @Override
    public P next() {
        this.requeue();
        if (this.remaining <= 0)
            throw new NoSuchElementException();
        this.remaining--;
        return this.current = this.queue.remove();
    }

    @Override
    public void remove() {
        if (this.current == null)
            throw new IllegalStateException();
        this.current = null;
    }

    private void requeue() {
        if (this.current != null) {
            this.queue.add(this.current);
            this.current = null;
        }
    }
}
