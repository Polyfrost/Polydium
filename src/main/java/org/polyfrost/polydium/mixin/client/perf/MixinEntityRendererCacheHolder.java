package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.polyfrost.polydium.client.perf.EntityRendererCacheHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Entity.class, BlockEntity.class})
public class MixinEntityRendererCacheHolder implements EntityRendererCacheHolder {
    @Unique
    private Entry polydium$rendererCache;

    @Override
    public Entry polydium$getRendererCache() {
        return this.polydium$rendererCache;
    }

    @Override
    public void polydium$setRendererCache(Entry entry) {
        this.polydium$rendererCache = entry;
    }
}
//?}
