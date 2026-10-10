package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.polyfrost.polydium.client.perf.EntityRendererCacheHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRendererCache {
    @Shadow
    private Map<EntityType<?>, EntityRenderer<?, ?>> renderers;

    @WrapOperation(
        method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
        require = 0
    )
    private Object polydium$cachedRenderer(Map<?, ?> map, Object type, Operation<Object> original, @Local(argsOnly = true) Entity entity) {
        if (map != this.renderers || type != entity.getType()) {
            return original.call(map, type);
        }
        return EntityRendererCacheHolder.lookup(entity, map, type, original);
    }
}
//?}
