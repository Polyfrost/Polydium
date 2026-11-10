package org.polyfrost.polydium.client.perf;

import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.compat.Mods;
import org.polyfrost.polydium.mixin.client.perf.BaseSpawnerSpinAccessor;
import org.polyfrost.polydium.mixin.client.perf.ChestLidControllerAccessor;
import org.polyfrost.polydium.mixin.client.perf.ChestLidHolderAccessor;
import org.polyfrost.polydium.mixin.client.perf.EnderChestLidHolderAccessor;

public final class IdleBlockEntityTicks {
    private static final boolean CHESTS = !Mods.loaded("lithium") && !Mods.loaded("obe");

    private IdleBlockEntityTicks() {
    }

    public static boolean isIdle(BlockEntity entity) {
        if (!PolydiumToggles.idleBlockEntityTicks)
            return false;
        Level level = entity.getLevel();
        if (level == null || !level.isClientSide())
            return false;

        Class<?> type = entity.getClass();
        if (type == SpawnerBlockEntity.class)
            return isStillAndAlone(level, entity, ((SpawnerBlockEntity) entity).getSpawner());
        if (!CHESTS)
            return false;
        if (type == ChestBlockEntity.class || type == TrappedChestBlockEntity.class)
            return isResting((ChestLidControllerAccessor) ((ChestLidHolderAccessor) entity).polydium$chestLidController());
        if (type == EnderChestBlockEntity.class)
            return isResting((ChestLidControllerAccessor) ((EnderChestLidHolderAccessor) entity).polydium$chestLidController());
        return false;
    }

    private static boolean isResting(ChestLidControllerAccessor lid) {
        float openness = lid.polydium$openness();
        boolean moving = lid.polydium$shouldBeOpen() ? openness < 1.0F : openness > 0.0F;
        return !moving && Float.floatToRawIntBits(lid.polydium$oOpenness()) == Float.floatToRawIntBits(openness);
    }

    private static boolean isStillAndAlone(Level level, BlockEntity entity, BaseSpawner spawner) {
        BaseSpawnerSpinAccessor spin = (BaseSpawnerSpinAccessor) spawner;
        return Double.doubleToRawLongBits(spin.polydium$oSpin()) == Double.doubleToRawLongBits(spin.polydium$spin())
            && !spin.polydium$isNearPlayer(level, entity.getBlockPos());
    }
}
