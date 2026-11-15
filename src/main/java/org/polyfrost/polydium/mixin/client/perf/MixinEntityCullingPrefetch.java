package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import org.polyfrost.polydium.client.perf.BlockEntityCullBox;
import org.polyfrost.polydium.client.perf.ChunkPrefetchCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.tr7zw.entityculling.EntityCullingModBase", remap = false)
public abstract class MixinEntityCullingPrefetch {
    @Shadow
    public Set<BlockEntityType<?>> blockEntityWhitelist;

    @Unique
    private int polydium$generation;
    @Unique
    private int polydium$rendererEpoch;
    @Unique
    private Set<BlockEntityType<?>> polydium$whitelist = Set.of();

    @Inject(method = "extractEntityData", at = @At("HEAD"), require = 0, expect = 0)
    private void polydium$checkPrefetchInputs(CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread())
            return;
        int epoch = BlockEntityCullBox.RENDERER_EPOCH.get();
        if (epoch != this.polydium$rendererEpoch || !this.polydium$whitelist.equals(this.blockEntityWhitelist)) {
            this.polydium$rendererEpoch = epoch;
            this.polydium$whitelist = Set.copyOf(this.blockEntityWhitelist);
            this.polydium$generation++;
        }
    }

    @WrapOperation(method = "extractEntityData", at = @At(value = "INVOKE", target = "Ldev/tr7zw/entityculling/EntityCullingModBase;prefetchBlockEntityData(Ljava/util/List;Ljava/util/Map;)V"), require = 0, expect = 0)
    private void polydium$cachedPrefetch(@Coerce Object mod, List<Object> out, Map<BlockPos, BlockEntity> blockEntities, Operation<Void> original, @Local LevelChunk chunk) {
        Minecraft client = Minecraft.getInstance();
        if (!client.isSameThread()) {
            original.call(mod, out, blockEntities);
            return;
        }
        ChunkPrefetchCache cache = (ChunkPrefetchCache) chunk;
        Object[] cached = cache.polydium$prefetch(this.polydium$generation);
        if (cached != null) {
            for (Object cullable : cached)
                out.add(cullable);
            return;
        }
        int start = out.size();
        original.call(mod, out, blockEntities);
        List<Object> added = out.subList(start, out.size());
        BlockEntityRenderDispatcher dispatcher = client.getBlockEntityRenderDispatcher();
        for (Object cullable : added) {
            if (!BlockEntityCullBox.fixedBounds(dispatcher.getRenderer((BlockEntity) cullable).getClass()))
                return;
        }
        cache.polydium$setPrefetch(added.toArray(), this.polydium$generation);
    }
}
//?}
