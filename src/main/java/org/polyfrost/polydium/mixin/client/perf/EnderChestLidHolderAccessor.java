package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnderChestBlockEntity.class)
public interface EnderChestLidHolderAccessor {
    @Accessor("chestLidController")
    ChestLidController polydium$chestLidController();
}
