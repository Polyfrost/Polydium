package org.polyfrost.polydium.client.perf;

//? if >= 1.21.4 {
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FullCubeShape {
    private static final Direction.Axis[] AXES = Direction.Axis.values();

    private FullCubeShape() {
    }

    public static VoxelShape canonical(VoxelShape face) {
        if (face == Shapes.block() || face.isEmpty()) {
            return face;
        }
        for (Direction.Axis axis : AXES) {
            if (face.min(axis) > 1.0E-7 || face.max(axis) < 1.0 - 1.0E-7) {
                return face;
            }
        }
        return Block.isShapeFullBlock(face) ? Shapes.block() : face;
    }
}
//?}
