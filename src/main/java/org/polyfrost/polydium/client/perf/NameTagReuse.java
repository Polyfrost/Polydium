package org.polyfrost.polydium.client.perf;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.IdentityHashMap;

public final class NameTagReuse {
    public static final int NO_WIDTH = Integer.MIN_VALUE;
    private static final int LIMIT = 4096;
    private static final long UNUSED_FRAMES = 200L;

    private static final IdentityHashMap<MutableComponent, Width> WIDTHS = new IdentityHashMap<>();
    private static int generation;
    private static long lastEviction;

    private NameTagReuse() {
    }

    public interface Memo {
        Component polydium$reuseNameTag(Component nameTag);

        Component polydium$reuseScoreText(Component scoreText);
    }

    public static boolean matches(Component remembered, Component fresh) {
        return remembered != null && remembered.equals(fresh);
    }

    public static Component remembered(Component fresh) {
        return fresh != null && isStatic(fresh) ? snapshot(fresh) : null;
    }

    private static MutableComponent snapshot(Component component) {
        ComponentContents contents = component.getContents();
        if (contents instanceof TranslatableContents translatable) {
            Object[] args = translatable.getArgs().clone();
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Component nested) {
                    args[i] = snapshot(nested);
                }
            }
            contents = new TranslatableContents(translatable.getKey(), translatable.getFallback(), args);
        }
        MutableComponent copy = MutableComponent.create(contents).setStyle(component.getStyle());
        for (Component sibling : component.getSiblings()) {
            copy.append(snapshot(sibling));
        }
        return copy;
    }

    public static boolean isStatic(Component component) {
        ComponentContents contents = component.getContents();
        if (contents instanceof TranslatableContents translatable) {
            for (Object argument : translatable.getArgs()) {
                if (argument instanceof Component nested && !isStatic(nested)) {
                    return false;
                }
            }
        } else if (!(contents instanceof PlainTextContents)) {
            return false;
        }
        for (Component sibling : component.getSiblings()) {
            if (!isStatic(sibling)) {
                return false;
            }
        }
        return true;
    }

    public static int width(Font font, MutableComponent component) {
        long frame = PerfClock.frame;
        int current = FontGeneration.get();
        if (current != generation) {
            WIDTHS.clear();
            generation = current;
        }
        if (frame - lastEviction >= UNUSED_FRAMES) {
            lastEviction = frame;
            WIDTHS.values().removeIf(width -> frame - width.frame > UNUSED_FRAMES);
        }
        Width cached = WIDTHS.get(component);
        if (cached == null || cached.font != font) {
            return NO_WIDTH;
        }
        cached.frame = frame;
        return cached.width;
    }

    public static void putWidth(Font font, MutableComponent component, int width) {
        if (WIDTHS.size() >= LIMIT) {
            WIDTHS.clear();
        }
        WIDTHS.put(component, new Width(font, width, PerfClock.frame));
    }

    private static final class Width {
        private final Font font;
        private final int width;
        private long frame;

        private Width(Font font, int width, long frame) {
            this.font = font;
            this.width = width;
            this.frame = frame;
        }
    }
}
