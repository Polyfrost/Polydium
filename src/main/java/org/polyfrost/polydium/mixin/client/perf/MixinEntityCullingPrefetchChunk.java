package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import net.minecraft.world.level.chunk.LevelChunk;
import org.polyfrost.polydium.client.perf.ChunkPrefetchCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class MixinEntityCullingPrefetchChunk implements ChunkPrefetchCache {
    @Unique
    private Object[] polydium$prefetch;
    @Unique
    private int polydium$prefetchGeneration;

    @Override
    public Object[] polydium$storedPrefetch() {
        return this.polydium$prefetch;
    }

    @Override
    public int polydium$prefetchGeneration() {
        return this.polydium$prefetchGeneration;
    }

    @Override
    public void polydium$setPrefetch(Object[] cullables, int generation) {
        this.polydium$prefetch = cullables;
        this.polydium$prefetchGeneration = generation;
    }

    @Inject(method = {"setBlockEntity", "removeBlockEntity", "clearAllBlockEntities"}, at = @At("HEAD"))
    private void polydium$invalidateOnChange(CallbackInfo ci) {
        this.polydium$invalidatePrefetch();
    }

    @Inject(method = "getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
    private void polydium$invalidateOnDrop(CallbackInfoReturnable<?> cir) {
        this.polydium$invalidatePrefetch();
    }
}
//?}
