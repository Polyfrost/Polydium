package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.polyfrost.polydium.compat.Mods;

public final class PrecipitationCache {
    private static final int LIMIT = 16384;
    private static final Biome.Precipitation[] VALUES = Biome.Precipitation.values();
    private static final boolean SEASONAL = Mods.SERENE_SEASONS || Mods.loaded("seasons");

    public interface Access {
        PrecipitationCache polydium$precipitationCache();
    }

    private final Long2ObjectOpenHashMap<Holder<Biome>> biomes = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<Holder<Biome>[]> sections = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<PrecipitationColumn> columns = new Long2ObjectOpenHashMap<>();
    private long lastChunk;
    private Holder<Biome>[] lastSections;
    private int generation;

    public Holder<Biome> biome(ClientLevel level, BlockPos pos, Operation<Holder<Biome>> original) {
        Holder<Biome> biome = this.uniformBiome(level, pos);
        if (biome != null) {
            return biome;
        }
        long key = pos.asLong();
        biome = this.biomes.get(key);
        if (biome == null) {
            if (this.biomes.size() >= LIMIT) {
                this.biomes.clear();
            }
            biome = original.call(level, pos);
            this.biomes.put(key, biome);
        }
        return biome;
    }

    private Holder<Biome> uniformBiome(ClientLevel level, BlockPos pos) {
        int quartX = PrecipitationColumn.quart(pos.getX());
        int quartY = PrecipitationColumn.quart(pos.getY());
        int quartZ = PrecipitationColumn.quart(pos.getZ());
        int minQuartY = level./*? if >= 1.21.4 {*/getMinY/*?} else {*//*getMinBuildHeight*//*?}*/() >> 2;
        Holder<Biome> result = null;
        for (int corner = 0; corner < 4; corner++) {
            Holder<Biome>[] chunk = this.sections(level, quartX + (corner & 1) >> 2, quartZ + (corner >> 1) >> 2);
            if (chunk == null) {
                return null;
            }
            for (int y = quartY; y <= quartY + 1; y++) {
                Holder<Biome> biome = chunk[PrecipitationColumn.section(y, minQuartY, chunk.length)];
                if (biome == null || result != null && biome != result) {
                    return null;
                }
                result = biome;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Holder<Biome>[] sections(ClientLevel level, int chunkX, int chunkZ) {
        long key = PrecipitationColumn.key(chunkX, chunkZ);
        if (this.lastSections != null && this.lastChunk == key) {
            return this.lastSections;
        }
        Holder<Biome>[] result = this.sections.get(key);
        if (result == null) {
            LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
            if (chunk == null) {
                return null;
            }
            LevelChunkSection[] chunkSections = chunk.getSections();
            result = new Holder[chunkSections.length];
            for (int i = 0; i < result.length; i++) {
                PalettedContainerRO<Holder<Biome>> biomes = chunkSections[i].getBiomes();
                Holder<Biome> first = biomes.get(0, 0, 0);
                if (!biomes.maybeHas(biome -> biome != first)) {
                    result[i] = first;
                }
            }
            this.sections.put(key, result);
        }
        this.lastChunk = key;
        this.lastSections = result;
        return result;
    }

    public Biome.Precipitation precipitation(Biome biome, BlockPos pos, int seaLevel, Operation<Biome.Precipitation> original) {
        if (SEASONAL) {
            return original.call(biome, pos/*? if >= 1.21.4 {*/, seaLevel/*?}*/);
        }
        long key = PrecipitationColumn.key(pos.getX(), pos.getZ());
        PrecipitationColumn column = this.columns.get(key);
        if (column == null) {
            if (this.columns.size() >= LIMIT) {
                this.columns.clear();
            }
            column = new PrecipitationColumn();
            this.columns.put(key, column);
        }
        int cached = column.get(biome, seaLevel, pos.getY());
        if (cached != PrecipitationColumn.UNKNOWN) {
            return VALUES[cached];
        }
        Biome.Precipitation precipitation = original.call(biome, pos/*? if >= 1.21.4 {*/, seaLevel/*?}*/);
        column.put(biome, seaLevel, pos.getY(), precipitation.ordinal(), precipitation == Biome.Precipitation.SNOW);
        return precipitation;
    }

    public int generation() {
        return this.generation;
    }

    public void clear() {
        this.biomes.clear();
        this.sections.clear();
        this.columns.clear();
        this.lastSections = null;
        this.generation++;
    }
}
//?}
