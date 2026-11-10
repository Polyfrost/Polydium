package org.polyfrost.polydium.client.perf;

import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

// Unordered iterator over a reusable list; remove() swaps the last element into the hole in O(1)
public final class SwapRemoveIterator<V> implements ObjectIterator<V> {
    public final ArrayList<V> list = new ArrayList<>();
    private final Consumer<V> onRemove;
    private int index;

    public SwapRemoveIterator(Consumer<V> onRemove) {
        this.onRemove = onRemove;
    }

    public SwapRemoveIterator<V> restart() {
        this.index = 0;
        return this;
    }

    @Override
    public boolean hasNext() {
        return this.index < this.list.size();
    }

    @Override
    public V next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        return this.list.get(this.index++);
    }

    @Override
    public void remove() {
        if (this.index == 0) {
            throw new IllegalStateException();
        }
        V removed = this.list.get(--this.index);
        V last = this.list.removeLast();
        if (this.index < this.list.size()) {
            this.list.set(this.index, last);
        }
        this.onRemove.accept(removed);
    }
}
