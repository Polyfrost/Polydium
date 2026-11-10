package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.world.level.block.entity.ChestLidController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestLidController.class)
public interface ChestLidControllerAccessor {
    @Accessor("shouldBeOpen")
    boolean polydium$shouldBeOpen();

    @Accessor("openness")
    float polydium$openness();

    @Accessor("oOpenness")
    float polydium$oOpenness();
}
