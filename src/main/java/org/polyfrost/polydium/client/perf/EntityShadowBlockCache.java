package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.lang.ref.WeakReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class EntityShadowBlockCache {
    private static final Entry NONE = new Entry(null, 0);
    private static final Long2ObjectOpenHashMap<Entry> ENTRIES = new Long2ObjectOpenHashMap<>();

    private static long tick = -1L;
    private static WeakReference<Level> level = new WeakReference<>(null);

    private EntityShadowBlockCache() {
    }

    public static Entry get(Level currentLevel, BlockPos pos, ChunkAccess chunk) {
        if (PerfClock.tick != tick || level.get() != currentLevel) {
            ENTRIES.clear();
            tick = PerfClock.tick;
            level = new WeakReference<>(currentLevel);
        }
        long key = pos.asLong();
        Entry entry = ENTRIES.get(key);
        if (entry == null) {
            entry = compute(currentLevel, pos, chunk);
            ENTRIES.put(key, entry);
        }
        return entry == NONE ? null : entry;
    }

    private static Entry compute(Level level, BlockPos pos, ChunkAccess chunk) {
        BlockPos belowPos = pos.below();
        BlockState belowState = chunk.getBlockState(belowPos);
        if (belowState.getRenderShape() == RenderShape.INVISIBLE) {
            return NONE;
        }
        int brightness = level.getMaxLocalRawBrightness(pos);
        if (brightness <= 3 || !belowState.isCollisionShapeFullBlock(chunk, belowPos)) {
            return NONE;
        }
        VoxelShape shape = belowState.getShape(chunk, belowPos);
        return shape.isEmpty() ? NONE : new Entry(shape, brightness);
    }

    public record Entry(VoxelShape shape, int brightness) {
    }
}
//?}
