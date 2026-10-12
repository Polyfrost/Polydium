package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {

import net.caffeinemc.mods.sodium.client.util.iterator.ByteIterator;

final class OcclusionSectionIterator implements ByteIterator {

    private static final int CAPACITY = 256;

    private final byte[] sections = new byte[CAPACITY];
    private int          count;
    private int          position;

    void reset() {
        this.count    = 0;
        this.position = 0;
    }

    void add(int section) {
        this.sections[this.count ++] = (byte) section;
    }

    boolean isEmpty() {
        return this.count == 0;
    }

    @Override
    public boolean hasNext() {
        return this.position < this.count;
    }

    @Override
    public int nextByteAsInt() {
        return this.sections[this.position ++] & 0xFF;
    }
}
//?}
