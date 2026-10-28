package org.polyfrost.polydium.client.perf;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Ported from Argentum with permission from rdh
public final class BlockEntityLight {
    public interface Access {
        int polydium$cachedLight(int generation, BlockState state);

        void polydium$cacheLight(int generation, BlockState state, int light);
    }

    private static final int LIMIT = Integer.getInteger("polydium.blockEntityLightSections", 1 << 19);

    private static final Long2IntOpenHashMap GENERATIONS = new Long2IntOpenHashMap();
    private static int counter;
    private static int floor;

    private BlockEntityLight() {
    }

    public static synchronized int generation(int blockX, int blockY, int blockZ) {
        return Math.max(GENERATIONS.get(key(blockX >> 4, blockY >> 4, blockZ >> 4)), floor);
    }

    public static void invalidateAround(int sectionX, int sectionY, int sectionZ) {
        invalidate(sectionX - 1, sectionY - 1, sectionZ - 1, sectionX + 1, sectionY + 1, sectionZ + 1);
    }

    public static synchronized void invalidate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (GENERATIONS.size() > LIMIT) {
            clear();
        }
        int generation = ++counter;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    GENERATIONS.put(key(x, y, z), generation);
                }
            }
        }
    }

    public static synchronized void forgetChunk(int chunkX, int chunkZ) {
        for (int y = -128; y < 128; y++) {
            GENERATIONS.remove(key(chunkX, y, chunkZ));
        }
    }

    public static synchronized void clear() {
        GENERATIONS.clear();
        GENERATIONS.trim();
        floor = ++counter;
    }

    private static long key(int sectionX, int sectionY, int sectionZ) {
        return (long) (sectionX & 0x3FFFFF) << 42 | (long) (sectionY & 0xFF) << 34 | (long) (sectionZ & 0x3FFFFF) << 12;
    }

    public static int generation(BlockEntity blockEntity) {
        if (!usable(blockEntity)) {
            return -1;
        }
        BlockPos pos = blockEntity.getBlockPos();
        return generation(pos.getX(), pos.getY(), pos.getZ());
    }

    public static int cached(BlockEntity blockEntity) {
        return cached(blockEntity, generation(blockEntity));
    }

    public static int cached(BlockEntity blockEntity, int generation) {
        return generation == -1 ? -1 : ((Access) blockEntity).polydium$cachedLight(generation, blockEntity.getBlockState());
    }

    public static void store(BlockEntity blockEntity, int generation, int light) {
        if (generation != -1) {
            ((Access) blockEntity).polydium$cacheLight(generation, blockEntity.getBlockState(), light);
        }
    }

    private static boolean usable(BlockEntity blockEntity) {
        return blockEntity instanceof Access && blockEntity.getLevel() == Minecraft.getInstance().level && RenderSystem.isOnRenderThread();
    }
}
