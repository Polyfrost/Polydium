package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.resources.Identifier;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UnifontCache {
    private static final int MAX_ENTRIES = 4;
    private static final Map<Key, Entry> CACHE = new ConcurrentHashMap<>();

    private UnifontCache() {
    }

    public static GlyphProvider find(Identifier hexFile, List<?> sizeOverrides, byte[] digest) {
        Entry cached = CACHE.get(new Key(hexFile, sizeOverrides));
        return cached != null && Arrays.equals(cached.digest(), digest) ? cached.provider() : null;
    }

    public static void remember(Identifier hexFile, List<?> sizeOverrides, byte[] digest, GlyphProvider provider) {
        Key key = new Key(hexFile, sizeOverrides);
        if (CACHE.size() >= MAX_ENTRIES && !CACHE.containsKey(key)) {
            CACHE.clear();
        }
        CACHE.put(key, new Entry(digest, provider));
    }

    public static void clear() {
        CACHE.clear();
    }

    public static byte[] digest(byte[] contents) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(contents);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private record Key(Identifier hexFile, List<?> sizeOverrides) {
    }

    private record Entry(byte[] digest, GlyphProvider provider) {
    }
}
//?}
