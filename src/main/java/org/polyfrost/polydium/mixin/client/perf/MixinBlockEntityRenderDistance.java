package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntityRenderer.class)
public interface MixinBlockEntityRenderDistance {
    @Shadow
    int getViewDistance();

    /**
     * @author Polydium
     * @reason Vanilla allocates a Vec3 per block entity per frame for the distance check.
     */
    @Overwrite
    default boolean shouldRender(BlockEntity blockEntity, Vec3 cameraPosition) {
        BlockPos pos = blockEntity.getBlockPos();
        double xd = cameraPosition.x() - (pos.getX() + 0.5);
        double yd = cameraPosition.y() - (pos.getY() + 0.5);
        double zd = cameraPosition.z() - (pos.getZ() + 0.5);
        double distance = this.getViewDistance();
        return xd * xd + yd * yd + zd * zd < distance * distance;
    }
}
//?}
