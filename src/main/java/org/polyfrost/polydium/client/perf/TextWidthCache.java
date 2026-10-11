package org.polyfrost.polydium.client.perf;

import java.util.HashMap;

public final class TextWidthCache {
    public static final int LIMIT = Integer.getInteger("polydium.textWidthCache", 4096);
    private static final int MAX_LENGTH = 256;

    public interface Access {
        void polydium$enableWidthCache();
    }

    private final HashMap<String, Integer> widths = new HashMap<>();
    private final int limit;
    private int generation;

    public TextWidthCache(int limit) {
        this.limit = limit;
    }

    public Integer get(String text, int generation) {
        if (generation != this.generation) {
            this.widths.clear();
            this.generation = generation;
            return null;
        }
        return text.length() > MAX_LENGTH ? null : this.widths.get(text);
    }

    public void put(String text, int generation, int width) {
        if (generation != this.generation || text.length() > MAX_LENGTH) {
            return;
        }
        if (this.widths.size() >= this.limit) {
            this.widths.clear();
        }
        this.widths.put(text, width);
    }
}
