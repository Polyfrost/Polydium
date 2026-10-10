package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.polyfrost.polydium.client.perf.EntityRendererCacheHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRendererCache {
    @Shadow
    private Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> renderers;

    @WrapOperation(
        method = "getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
        require = 0
    )
    private Object polydium$cachedRenderer(Map<?, ?> map, Object type, Operation<Object> original, @Local(argsOnly = true) BlockEntity blockEntity) {
        if (map != this.renderers || type != blockEntity.getType()) {
            return original.call(map, type);
        }
        return EntityRendererCacheHolder.lookup(blockEntity, map, type, original);
    }
}
//?}
