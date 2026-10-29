package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Display.class)
public abstract class MixinDisplayViewRange {
    @Shadow
    @Final
    private static EntityDataAccessor<Float> DATA_VIEW_RANGE_ID;

    @Unique
    private float polydium$viewRange;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$initViewRange(CallbackInfo ci) {
        this.polydium$storeViewRange();
    }

    @Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("HEAD"))
    private void polydium$updateViewRange(EntityDataAccessor<?> accessor, CallbackInfo ci) {
        if (DATA_VIEW_RANGE_ID.equals(accessor))
            this.polydium$storeViewRange();
    }

    @WrapOperation(method = "shouldRenderAtSqrDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Display;getViewRange()F"))
    private float polydium$storedViewRange(Display display, Operation<Float> original) {
        return PolydiumToggles.syncedDataCopies ? this.polydium$viewRange : original.call(display);
    }

    @Unique
    private void polydium$storeViewRange() {
        this.polydium$viewRange = ((Entity) (Object) this).getEntityData().get(DATA_VIEW_RANGE_ID);
    }
}
