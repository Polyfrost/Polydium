package org.polyfrost.polydium.mixin.client.perf;


//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.EntityCullLerp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class MixinEntityCullPartialTick {
    @ModifyArg(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;isEntityVisible(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"), index = 0)
    private Entity polydium$cullAtPartialTick(Entity entity, @Local(argsOnly = true) DeltaTracker deltaTracker, @Local TickRateManager tickRateManager) {
        EntityCullLerp.begin(deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity)));
        return entity;
    }

    @Inject(method = "extractVisibleEntities", at = @At("RETURN"))
    private void polydium$endCullLerp(CallbackInfo ci) {
        EntityCullLerp.end();
    }
}
*///?} else if >= 1.21.4 && < 1.21.10 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.EntityCullLerp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public abstract class MixinEntityCullPartialTick {
    @ModifyArg(method = "collectVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"), index = 0)
    private Entity polydium$cullAtPartialTick(Entity entity) {
        EntityCullLerp.begin(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(!entity.level().tickRateManager().isEntityFrozen(entity)));
        return entity;
    }

    @Inject(method = "collectVisibleEntities", at = @At("RETURN"))
    private void polydium$endCullLerp(CallbackInfoReturnable<Boolean> cir) {
        EntityCullLerp.end();
    }
}
*///?} else if < 26.2 {
/*import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.EntityCullLerp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinEntityCullPartialTick {
    @ModifyArg(method = /^? if >= 1.21.10 {^/"extractVisibleEntities"/^?} else {^//^"renderLevel"^//^?}^/, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"), index = 0)
    private Entity polydium$cullAtPartialTick(Entity entity, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        EntityCullLerp.begin(deltaTracker.getGameTimeDeltaPartialTick(!entity.level().tickRateManager().isEntityFrozen(entity)));
        return entity;
    }

    @Inject(method = /^? if >= 1.21.10 {^/"extractVisibleEntities"/^?} else {^//^"renderLevel"^//^?}^/, at = @At("RETURN"))
    private void polydium$endCullLerp(CallbackInfo ci) {
        EntityCullLerp.end();
    }
}
*///?}
