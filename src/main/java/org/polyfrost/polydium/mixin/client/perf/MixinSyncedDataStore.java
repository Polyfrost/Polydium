package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import org.polyfrost.polydium.client.perf.SyncedDataCopy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SynchedEntityData.class)
public abstract class MixinSyncedDataStore {
    @Shadow
    @Final
    private SyncedDataHolder entity;

    @WrapOperation(method = {"set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;Z)V", "assignValue"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData$DataItem;setValue(Ljava/lang/Object;)V"))
    private void polydium$storeValue(SynchedEntityData.DataItem<?> dataItem, Object value, Operation<Void> original) {
        original.call(dataItem, value);

        if (this.entity instanceof SyncedDataCopy copy)
            copy.polydium$storeSyncedValue(dataItem.getAccessor(), value);
    }
}
