package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import java.util.Map;

public interface EntityRendererCacheHolder {
    Entry polydium$getRendererCache();

    void polydium$setRendererCache(Entry entry);

    record Entry(Map<?, ?> source, Object renderer) {
    }

    static Object lookup(Object owner, Map<?, ?> source, Object key, Operation<Object> original) {
        EntityRendererCacheHolder holder = (EntityRendererCacheHolder) owner;
        Entry entry = holder.polydium$getRendererCache();
        if (entry != null && entry.source == source) {
            return entry.renderer;
        }
        Object renderer = original.call(source, key);
        if (renderer != null) {
            holder.polydium$setRendererCache(new Entry(source, renderer));
        }
        return renderer;
    }
}
//?}
