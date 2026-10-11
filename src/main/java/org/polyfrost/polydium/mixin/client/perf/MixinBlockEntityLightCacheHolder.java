package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.polyfrost.polydium.client.perf.BlockEntityLight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntity.class)
public class MixinBlockEntityLightCacheHolder implements BlockEntityLight.Access {
    @Unique
    private BlockState polydium$lightState;
    @Unique
    private int polydium$lightGeneration;
    @Unique
    private int polydium$light;

    @Override
    public int polydium$cachedLight(int generation, BlockState state) {
        return this.polydium$lightState == state && this.polydium$lightGeneration == generation ? this.polydium$light : -1;
    }

    @Override
    public void polydium$cacheLight(int generation, BlockState state, int light) {
        this.polydium$lightState = state;
        this.polydium$lightGeneration = generation;
        this.polydium$light = light;
    }
}
