package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.client.perf.BlockEntityCullBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntityCullBox implements BlockEntityCullBox {
    @Unique
    private AABB polydium$cullBox;
    @Unique
    private int polydium$cullBoxEpoch;

    @Override
    public AABB polydium$storedCullBox() {
        return this.polydium$cullBox;
    }

    @Override
    public int polydium$cullBoxEpoch() {
        return this.polydium$cullBoxEpoch;
    }

    @Override
    public void polydium$setCullBox(AABB box, int rendererEpoch) {
        this.polydium$cullBox = box;
        this.polydium$cullBoxEpoch = rendererEpoch;
    }
}
