package org.polyfrost.polydium.client.perf;

public final class PrecipitationColumn {
    public static final int UNKNOWN = -1;
    private static final int SNOW_LEVEL_OFFSET = 17;

    private Object biome;
    private int seaLevel;
    private int low = UNKNOWN;
    private int warm = UNKNOWN;
    private int warmTop = Integer.MIN_VALUE;
    private int cold = UNKNOWN;
    private int coldBottom = Integer.MAX_VALUE;

    public static long key(int x, int z) {
        return (long) x << 32 | z & 0xFFFFFFFFL;
    }

    public static int quart(int block) {
        return block - 2 >> 2;
    }

    public static int section(int quartY, int minQuartY, int sections) {
        return Math.max(0, Math.min(quartY - minQuartY, sections * 4 - 1)) >> 2;
    }

    public int get(Object biome, int seaLevel, int y) {
        if (this.biome != biome || this.seaLevel != seaLevel) {
            return UNKNOWN;
        }
        if (y <= seaLevel + SNOW_LEVEL_OFFSET) {
            return this.low;
        }
        if (y <= this.warmTop) {
            return this.warm;
        }
        return y >= this.coldBottom ? this.cold : UNKNOWN;
    }

    public void put(Object biome, int seaLevel, int y, int value, boolean snow) {
        if (this.biome != biome || this.seaLevel != seaLevel) {
            this.biome = biome;
            this.seaLevel = seaLevel;
            this.low = this.warm = this.cold = UNKNOWN;
            this.warmTop = Integer.MIN_VALUE;
            this.coldBottom = Integer.MAX_VALUE;
        }
        if (y <= seaLevel + SNOW_LEVEL_OFFSET) {
            this.low = value;
        } else if (snow) {
            this.cold = value;
            this.coldBottom = Math.min(this.coldBottom, y);
        } else {
            this.warm = value;
            this.warmTop = Math.max(this.warmTop, y);
        }
    }
}
