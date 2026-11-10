package org.polyfrost.polydium.mixin.client.perf;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.SyncedDataCopy;
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Entity.class, priority = 999)
public abstract class MixinEntitySyncedDataCopy implements SyncedDataCopy {
    @Unique
    private static final boolean polydium$STORED = !PolydiumMixinPlugin.isDisabled("org.polyfrost.polydium.mixin.client.perf.MixinSyncedDataStore");

    @Shadow
    @Final
    protected static EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
    @Shadow
    @Final
    private static EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME;
    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE;
    @Shadow
    @Final
    protected SynchedEntityData entityData;

    @Unique
    private byte polydium$sharedFlags;
    @Unique
    private Component polydium$customName;
    @Unique
    private boolean polydium$customNameVisible;

    @Override
    public void polydium$storeSyncedValue(EntityDataAccessor<?> accessor, Object value) {
        if (accessor == DATA_SHARED_FLAGS_ID)
            this.polydium$sharedFlags = (Byte) value;
        else if (accessor == DATA_CUSTOM_NAME)
            this.polydium$customName = (Component) ((Optional<?>) value).orElse(null);
        else if (accessor == DATA_CUSTOM_NAME_VISIBLE)
            this.polydium$customNameVisible = (Boolean) value;
    }

    @Unique
    private static boolean polydium$copies() {
        return polydium$STORED && PolydiumToggles.syncedDataCopies;
    }

    @Overwrite
    protected boolean getSharedFlag(int flag) {
        if (polydium$copies())
            return (this.polydium$sharedFlags & 1 << flag) != 0;
        return (this.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << flag) != 0;
    }

    @Overwrite
    public Component getCustomName() {
        if (polydium$copies())
            return this.polydium$customName;
        return this.entityData.get(DATA_CUSTOM_NAME).orElse(null);
    }

    @Overwrite
    public boolean hasCustomName() {
        if (polydium$copies())
            return this.polydium$customName != null;
        return this.entityData.get(DATA_CUSTOM_NAME).isPresent();
    }

    @Overwrite
    public boolean isCustomNameVisible() {
        if (polydium$copies())
            return this.polydium$customNameVisible;
        return this.entityData.get(DATA_CUSTOM_NAME_VISIBLE);
    }
}
