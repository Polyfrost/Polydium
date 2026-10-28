package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ParticleAirCollision {
    private static final double EPSILON = 1.0E-7;
    private static final int TYPE_FACE = 1;
    private static final int TYPE_EDGE = 2;
    private static final int TYPE_CORNER = 3;

    private ParticleAirCollision() {
    }

    public static boolean cannotCollide(Level level, CollisionContext context, AABB box) {
        if (level.isDebug())
            return false;
        int minX = Mth.floor(box.minX - EPSILON) - 1;
        int maxX = Mth.floor(box.maxX + EPSILON) + 1;
        int minY = Mth.floor(box.minY - EPSILON) - 1;
        int maxY = Mth.floor(box.maxY + EPSILON) + 1;
        int minZ = Mth.floor(box.minZ - EPSILON) - 1;
        int maxZ = Mth.floor(box.maxZ + EPSILON) + 1;
        BlockPos.MutableBlockPos pos = null;
        ClientChunkCache clientChunks = level instanceof ClientLevel clientLevel ? clientLevel.getChunkSource() : null;

        int maxChunkX = SectionPos.blockToSectionCoord(maxX);
        int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);
        for (int chunkX = SectionPos.blockToSectionCoord(minX); chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(minZ); chunkZ <= maxChunkZ; chunkZ++) {
                BlockGetter getter = clientChunks != null ? clientChunks.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) : level.getChunkForCollisions(chunkX, chunkZ);
                if (getter == null)
                    continue;
                if (!(getter instanceof ChunkAccess chunk))
                    return false;

                int fromX = Math.max(minX, chunkX << 4), toX = Math.min(maxX, (chunkX << 4) + 15);
                int fromZ = Math.max(minZ, chunkZ << 4), toZ = Math.min(maxZ, (chunkZ << 4) + 15);
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
                    if (section.hasOnlyAir())
                        continue;

                    int inset = mayReachNeighborCells(section) ? 0 : 1;
                    int fromY = Math.max(Math.max(bottom, minY + inset), sectionY << 4);
                    int toY = Math.min(Math.min(top, maxY - inset), (sectionY << 4) + 15);
                    int insetToZ = Math.min(toZ, maxZ - inset);
                    int insetToX = Math.min(toX, maxX - inset);
                    for (int y = fromY; y <= toY; y++) {
                        int yFaces = y == minY || y == maxY ? 1 : 0;
                        for (int z = Math.max(fromZ, minZ + inset); z <= insetToZ; z++) {
                            int yzFaces = yFaces + (z == minZ || z == maxZ ? 1 : 0);
                            for (int x = Math.max(fromX, minX + inset); x <= insetToX; x++) {
                                int type = yzFaces + (x == minX || x == maxX ? 1 : 0);
                                if (type == TYPE_CORNER)
                                    continue;
                                BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                                if (state.isAir() || type == TYPE_FACE && !state.hasLargeCollisionShape() || type == TYPE_EDGE && !state.is(Blocks.MOVING_PISTON))
                                    continue;
                                if (pos == null)
                                    pos = new BlockPos.MutableBlockPos();
                                pos.set(x, y, z);
                                //? if >= 26.2 {
                                VoxelShape shape = context.getCollisionShape(state, level, pos);
                                //?} else {
                                /*VoxelShape shape = state.getCollisionShape(level, pos, context);
                                *///?}
                                if (mayCollide(shape, box, x, y, z))
                                    return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public interface Reach {
        int polydium$paletteVersion();

        int polydium$reachCode();

        void polydium$setReachCode(int code);
    }

    private static boolean mayReachNeighborCells(LevelChunkSection section) {
        Reach reach = (Reach) section.getStates();
        int version = reach.polydium$paletteVersion() & 0x3FFFFFFF;
        int code = reach.polydium$reachCode();
        if (code >>> 1 == version)
            return (code & 1) != 0;
        boolean reaches = section.maybeHas(ParticleAirCollision::reachesNeighborCells);
        reach.polydium$setReachCode(version << 1 | (reaches ? 1 : 0));
        return reaches;
    }

    private static boolean reachesNeighborCells(BlockState state) {
        return state.hasLargeCollisionShape() || state.is(Blocks.MOVING_PISTON);
    }

    private static boolean mayCollide(VoxelShape shape, AABB box, int x, int y, int z) {
        if (shape == Shapes.block())
            return box.intersects(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        return !shape.isEmpty()
            && shape.min(Direction.Axis.X) + x < box.maxX && shape.max(Direction.Axis.X) + x > box.minX
            && shape.min(Direction.Axis.Y) + y < box.maxY && shape.max(Direction.Axis.Y) + y > box.minY
            && shape.min(Direction.Axis.Z) + z < box.maxZ && shape.max(Direction.Axis.Z) + z > box.minZ;
    }
}
//?}
