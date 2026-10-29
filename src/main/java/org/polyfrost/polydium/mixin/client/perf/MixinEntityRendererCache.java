package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
//? if >= 1.21.5 {
import net.minecraft.client.renderer.entity.state.EntityRenderState;
//?}
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.polyfrost.polydium.client.perf.EntityRendererCacheHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRendererCache {
    @Shadow
    private Map<EntityType<?>, ?> renderers;

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

    //? if >= 1.21.5 {
    @WrapOperation(
        method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
        require = 0
    )
    private Object polydium$cachedStateRenderer(Map<?, ?> map, Object type, Operation<Object> original, @Local(argsOnly = true) EntityRenderState state) {
        if (type == null || map != this.renderers || type != state.entityType) {
            return original.call(map, type);
        }
        return EntityRendererCacheHolder.lookup(type, map, type, original);
    }
    //?}

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void polydium$clearTypeCaches(ResourceManager manager, CallbackInfo ci) {
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE)
            ((EntityRendererCacheHolder) type).polydium$setRendererCache(null);
    }
}
//?}
