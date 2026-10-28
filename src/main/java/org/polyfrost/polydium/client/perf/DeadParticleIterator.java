package org.polyfrost.polydium.client.perf;

import net.minecraft.client.particle.Particle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class DeadParticleIterator<P> implements Iterator<P> {
    private final List<P> dead;
    private int next;

    private DeadParticleIterator(List<P> dead) {
        this.dead = dead;
    }

    public static <P> Iterator<P> of(Iterable<P> particles) {
        if (!(particles instanceof ArrayDeque<P> queue))
            return null;
        List<P> dead = new ArrayList<>();
        queue.removeIf(p -> p instanceof Particle particle && !particle.isAlive() && dead.add(p));
        return new DeadParticleIterator<>(dead);
    }

    @Override
    public boolean hasNext() {
        return this.next < this.dead.size();
    }

    @Override
    public P next() {
        if (this.next >= this.dead.size())
            throw new NoSuchElementException();
        return this.dead.get(this.next++);
    }

    @Override
    public void remove() {
    }
}
