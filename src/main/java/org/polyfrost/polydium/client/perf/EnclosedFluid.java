package org.polyfrost.polydium.client.perf;

//? if >= 26.1 {
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
//?} else {
/*import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class EnclosedFluid {
    private static final Direction[] NEIGHBORS = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private EnclosedFluid() {
    }

    public static boolean enclosed(BlockGetter level, BlockPos pos, FluidState fluidState, BlockPos.MutableBlockPos neighbor) {
        Fluid fluid = fluidState.getType();

        if (!fluid.isSame(Fluids.WATER) && !fluid.isSame(Fluids.LAVA))
            return false;

        //? if >= 26.1 {
        if (FluidRenderingRegistry.getOverride(fluid) != null)
        //?} else
        /*if (FluidRenderHandlerRegistry.INSTANCE.getOverride(fluid) != null)*/
            return false;

        for (Direction direction : NEIGHBORS) {
            if (!level.getBlockState(neighbor.setWithOffset(pos, direction)).getFluidState().getType().isSame(fluid))
                return false;
        }

        return true;
    }
}
