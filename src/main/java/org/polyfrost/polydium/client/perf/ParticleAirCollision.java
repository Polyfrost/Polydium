package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ParticleAirCollision {
    private static final double EPSILON = 1.0E-7;

    private ParticleAirCollision() {
    }

    public static boolean isOnlyAir(Level level, AABB box, Vec3 movement) {
        double dx = movement.x, dy = movement.y, dz = movement.z;
        int minX = Mth.floor(box.minX + Math.min(dx, 0.0) - EPSILON) - 1;
        int maxX = Mth.floor(box.maxX + Math.max(dx, 0.0) + EPSILON) + 1;
        int minY = Mth.floor(box.minY + Math.min(dy, 0.0) - EPSILON) - 1;
        int maxY = Mth.floor(box.maxY + Math.max(dy, 0.0) + EPSILON) + 1;
        int minZ = Mth.floor(box.minZ + Math.min(dz, 0.0) - EPSILON) - 1;
        int maxZ = Mth.floor(box.maxZ + Math.max(dz, 0.0) + EPSILON) + 1;

        int maxChunkX = SectionPos.blockToSectionCoord(maxX);
        int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);
        for (int chunkX = SectionPos.blockToSectionCoord(minX); chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(minZ); chunkZ <= maxChunkZ; chunkZ++) {
                BlockGetter getter = level.getChunkForCollisions(chunkX, chunkZ);
                if (getter == null) {
                    continue;
                }
                if (!(getter instanceof ChunkAccess chunk)) {
                    return false;
                }
                if (!isChunkAir(chunk,
                        Math.max(minX, chunkX << 4), Math.min(maxX, (chunkX << 4) + 15),
                        minY, maxY,
                        Math.max(minZ, chunkZ << 4), Math.min(maxZ, (chunkZ << 4) + 15))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isChunkAir(ChunkAccess chunk, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        //? if >= 1.21.4 {
        int bottom = Math.max(minY, chunk.getMinY());
        int top = Math.min(maxY, chunk.getMaxY());
        //?} else {
        /*int bottom = Math.max(minY, chunk.getMinBuildHeight());
        int top = Math.min(maxY, chunk.getMaxBuildHeight() - 1);
        *///?}
        LevelChunkSection[] sections = chunk.getSections();

        int maxSectionY = SectionPos.blockToSectionCoord(top);
        for (int sectionY = SectionPos.blockToSectionCoord(bottom); sectionY <= maxSectionY; sectionY++) {
            LevelChunkSection section = sections[chunk.getSectionIndexFromSectionY(sectionY)];
            if (section.hasOnlyAir()) {
                continue;
            }

            int fromY = Math.max(bottom, sectionY << 4);
            int toY = Math.min(top, (sectionY << 4) + 15);
            for (int y = fromY; y <= toY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int x = minX; x <= maxX; x++) {
                        if (!section.getBlockState(x & 15, y & 15, z & 15).isAir()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
//?}
