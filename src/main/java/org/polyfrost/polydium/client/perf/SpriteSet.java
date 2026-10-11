package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class SpriteSet {
    private static int generation;

    private SpriteSet() {
    }

    public interface Access {
        TextureAtlasSprite[] polydium$uniqueSprites();

        void polydium$forgetSprites();
    }

    public static int generation() {
        return generation;
    }

    public static void invalidate() {
        generation++;
    }

    public static <T> void add(ReferenceOpenHashSet<T> set, T[] sprites) {
        if (sprites == null) {
            return;
        }
        for (T sprite : sprites) {
            if (sprite != null) {
                set.add(sprite);
            }
        }
    }
}
//?}
