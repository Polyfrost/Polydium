package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityDistance {
    private static final ClassValue<Boolean> INHERITED = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            try {
                return type.getMethod("shouldRender", BlockEntity.class, Vec3.class).getDeclaringClass() == BlockEntityRenderer.class;
            } catch (Throwable ignored) {
                return false;
            }
        }
    };

    private BlockEntityDistance() {
    }

    public static boolean inheritsDefault(BlockEntityRenderer<?/*? if >= 1.21.10 {*/, ?/*?}*/> renderer) {
        return INHERITED.get(renderer.getClass());
    }

    public static boolean shouldRender(BlockEntityRenderer<?/*? if >= 1.21.10 {*/, ?/*?}*/> renderer, BlockEntity blockEntity, Vec3 cameraPosition) {
        BlockPos pos = blockEntity.getBlockPos();
        double xd = cameraPosition.x() - (pos.getX() + 0.5);
        double yd = cameraPosition.y() - (pos.getY() + 0.5);
        double zd = cameraPosition.z() - (pos.getZ() + 0.5);
        double distance = renderer.getViewDistance();
        return xd * xd + yd * yd + zd * zd < distance * distance;
    }
}
//?}
