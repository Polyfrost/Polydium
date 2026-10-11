package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

// Geometry-cache idea ported from Argentum (TextBatcher), with permission from rdh.
public final class PreparedTextCache<V> {
    public static final int LIMIT = Integer.getInteger("polydium.preparedTextCache", 2048);
    private static final int MAX_LENGTH = 256;

    public static boolean cacheable(Style style) {
        return !style.isObfuscated() /*? if >= 1.21.10 {*/&& style.getFont() instanceof FontDescription.Resource/*?}*/;
    }

    public interface Access {
        void polydium$enablePreparedTextCache();
    }

    public static final class Key {
        private int[] chars;
        private Object[] styles;
        private int length;
        private int x;
        private int y;
        private int color;
        private int background;
        private int flags;
        private int hash;

        public int length() {
            return this.length;
        }

        public int position(int index) {
            return this.chars[2 * index];
        }

        public int codePoint(int index) {
            return this.chars[2 * index + 1];
        }

        public Object style(int index) {
            return this.styles[index];
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Key key) || this.hash != key.hash || this.length != key.length || this.x != key.x || this.y != key.y
                    || this.color != key.color || this.background != key.background || this.flags != key.flags) {
                return false;
            }
            for (int i = 0, end = 2 * this.length; i < end; i++) {
                if (this.chars[i] != key.chars[i]) {
                    return false;
                }
            }
            for (int i = 0; i < this.length; i++) {
                Object style = this.styles[i];
                if (style != key.styles[i] && !style.equals(key.styles[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final Object SEEN = new Object();
    private static final Object MUTABLE = new Object();
    private static final ClassValue<Field[]> CAPTURED = new ClassValue<>() {
        @Override
        protected Field[] computeValue(Class<?> type) {
            if (type.getNestHost() != FormattedCharSequence.class) {
                return null;
            }
            try {
                Field[] fields = type.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                }
                return fields;
            } catch (Throwable t) {
                return null;
            }
        }
    };

    private record Seen(Object text, int x, int y, int color, int background, int flags) {
    }

    private final LinkedHashMap<Key, V> entries;
    private final LinkedHashMap<Seen, Object> seen;
    private Seen pending;
    private final Key scratch = new Key();
    private int generation;
    private int runHash;
    private Object lastStyle;

    public PreparedTextCache(int limit) {
        this.entries = lru(limit);
        this.seen = lru(limit);
        this.scratch.chars = new int[2 * MAX_LENGTH];
        this.scratch.styles = new Object[MAX_LENGTH];
    }

    private static <K, T> LinkedHashMap<K, T> lru(int limit) {
        return new LinkedHashMap<>(64, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, T> eldest) {
                return this.size() > limit;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public V seen(Object text, float x, float y, int color, int background, int flags) {
        Seen key = new Seen(text, Float.floatToIntBits(x), Float.floatToIntBits(y), color, background, flags);
        Object value = this.seen.get(key);
        this.pending = null;
        if (value == null) {
            this.seen.put(key, SEEN);
        } else if (value == SEEN) {
            this.seen.put(key, MUTABLE);
            if (immutable(text)) {
                this.pending = key;
            }
        } else if (value != MUTABLE) {
            return (V) value;
        }
        return null;
    }

    public V remember(V value) {
        if (this.pending != null) {
            this.seen.put(this.pending, value);
            this.pending = null;
        }
        return value;
    }

    private static boolean immutable(Object value) {
        if (value instanceof String || value instanceof Style || value instanceof Integer) {
            return true;
        }
        if (value instanceof ImmutableList<?> list) {
            for (Object element : list) {
                if (!immutable(element)) {
                    return false;
                }
            }
            return true;
        }
        if (!(value instanceof FormattedCharSequence)) {
            return false;
        }
        Field[] captured = CAPTURED.get(value.getClass());
        if (captured == null) {
            return false;
        }
        try {
            for (Field field : captured) {
                if (!Modifier.isStatic(field.getModifiers()) && !immutable(field.get(value))) {
                    return false;
                }
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public void begin(int generation) {
        if (generation != this.generation) {
            this.entries.clear();
            this.seen.clear();
            this.generation = generation;
        }
        this.scratch.length = 0;
        this.runHash = 0;
        this.lastStyle = null;
    }

    public boolean add(int position, Object style, int codePoint) {
        Key key = this.scratch;
        int length = key.length;
        if (length == MAX_LENGTH) {
            return false;
        }
        key.chars[2 * length] = position;
        key.chars[2 * length + 1] = codePoint;
        key.styles[length] = style;
        key.length = length + 1;
        if (style != this.lastStyle && !style.equals(this.lastStyle)) {
            this.runHash = 31 * this.runHash + style.hashCode();
        }
        this.lastStyle = style;
        this.runHash = 31 * (31 * this.runHash + position) + codePoint;
        return true;
    }

    public V get(float x, float y, int color, int background, int flags) {
        Key key = this.scratch;
        key.x = Float.floatToIntBits(x);
        key.y = Float.floatToIntBits(y);
        key.color = color;
        key.background = background;
        key.flags = flags;
        key.hash = 31 * (31 * (31 * (31 * (31 * this.runHash + key.x) + key.y) + color) + background) + flags;
        return this.entries.get(key);
    }

    public Key miss() {
        Key scratch = this.scratch;
        Key key = new Key();
        key.chars = Arrays.copyOf(scratch.chars, 2 * scratch.length);
        key.styles = Arrays.copyOf(scratch.styles, scratch.length);
        key.length = scratch.length;
        key.x = scratch.x;
        key.y = scratch.y;
        key.color = scratch.color;
        key.background = scratch.background;
        key.flags = scratch.flags;
        key.hash = scratch.hash;
        return key;
    }

    public void put(Key key, V value) {
        this.entries.put(key, value);
    }
}
