package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 && < 26.3 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.EntityCullLerp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class MixinHitboxCullPartialTick {
    @ModifyArg(method = "emitGizmos", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;isEntityVisible(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"), index = 0)
    private Entity polydium$cullAtPartialTick(Entity entity) {
        EntityCullLerp.begin(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(!entity.level().tickRateManager().isEntityFrozen(entity)));
        return entity;
    }

    @Inject(method = "emitGizmos", at = @At("RETURN"))
    private void polydium$endCullLerp(CallbackInfo ci) {
        EntityCullLerp.end();
    }
}
*///?}
