package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.polyfrost.polydium.client.perf.EntityRendererCacheHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRendererCache {
    @Shadow
    private Map<BlockEntityType<?>, ?> renderers;

    @WrapOperation(
        method = "getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
        require = 0
    )
    private Object polydium$cachedRenderer(Map<?, ?> map, Object type, Operation<Object> original) {
        if (map != this.renderers || !(type instanceof BlockEntityType<?>)) {
            return original.call(map, type);
        }
        return EntityRendererCacheHolder.lookup(type, map, type, original);
    }

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void polydium$clearTypeCaches(ResourceManager manager, CallbackInfo ci) {
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE)
            ((EntityRendererCacheHolder) type).polydium$setRendererCache(null);
    }
}
//?}
