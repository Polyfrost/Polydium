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
    private Object polydium$cullBoxRenderer;

    @Override
    public AABB polydium$storedCullBox() {
        return this.polydium$cullBox;
    }

    @Override
    public Object polydium$cullBoxRenderer() {
        return this.polydium$cullBoxRenderer;
    }

    @Override
    public void polydium$setCullBox(AABB box, Object renderer) {
        this.polydium$cullBox = box;
        this.polydium$cullBoxRenderer = renderer;
    }
}
