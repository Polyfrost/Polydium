package org.polyfrost.polydium.client.perf;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface SyncedDataCopy {
    void polydium$storeSyncedValue(EntityDataAccessor<?> accessor, Object value);
}
