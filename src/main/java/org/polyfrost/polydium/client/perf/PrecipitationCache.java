package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public final class PrecipitationCache {
    private static final int LIMIT = 16384;

    public interface Access {
        PrecipitationCache polydium$precipitationCache();
    }

    private static final class Entry {
        final long pos;
        final Holder<Biome> biome;
        Biome.Precipitation precipitation;

        Entry(long pos, Holder<Biome> biome) {
            this.pos = pos;
            this.biome = biome;
        }
    }

    private final Long2ObjectOpenHashMap<Entry> entries = new Long2ObjectOpenHashMap<>();
    private Entry last;

    public Holder<Biome> biome(ClientLevel level, BlockPos pos, Operation<Holder<Biome>> original) {
        long key = pos.asLong();
        Entry entry = this.entries.get(key);
        if (entry == null) {
            if (this.entries.size() >= LIMIT) {
                this.entries.clear();
            }
            entry = new Entry(key, original.call(level, pos));
            this.entries.put(key, entry);
        }
        this.last = entry;
        return entry.biome;
    }

    public Biome.Precipitation precipitation(Biome biome, BlockPos pos, int seaLevel, Operation<Biome.Precipitation> original) {
        Entry entry = this.last;
        if (entry == null || entry.pos != pos.asLong() || entry.biome.value() != biome) {
            return original.call(biome, pos, seaLevel);
        }
        if (entry.precipitation == null) {
            entry.precipitation = original.call(biome, pos, seaLevel);
        }
        return entry.precipitation;
    }

    public void clear() {
        this.entries.clear();
        this.last = null;
    }
}
//?}
