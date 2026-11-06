package org.polyfrost.polydium.client.perf;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//? if < 1.21.5 {
/*import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.WeakHashMap;
*///?}

public final class SpriteTransparency {
    //? if < 1.21.5 {
    /*private static final Map<BakedModel, Boolean> MODELS = new WeakHashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();
    *///?}

    private SpriteTransparency() {
    }

    public interface Holder {
        boolean polydium$hasTranslucent();
    }

    public static boolean hasTranslucent(TextureAtlasSprite sprite) {
        //? if >= 26.1 {
        return sprite.transparency().hasTranslucent();
        //?} else {
        /*return ((Holder) sprite.contents()).polydium$hasTranslucent();
        *///?}
    }

    //? if < 1.21.5 {
    /*/^* True when the model has a quad with a translucent sprite, no quads, or emits geometry the quad lists can't show. ^/
    public static boolean modelHasTranslucent(BakedModel model) {
        Boolean cached = MODELS.get(model);
        if (cached == null) {
            cached = scan(model);
            MODELS.put(model, cached);
        }
        return cached;
    }

    private static boolean scan(BakedModel model) {
        if (!((FabricBakedModel) model).isVanillaAdapter()) {
            return true;
        }
        boolean empty = true;
        for (int i = 0; i <= 6; i++) {
            RANDOM.setSeed(42L);
            for (BakedQuad quad : model.getQuads(null, i == 6 ? null : Direction.values()[i], RANDOM)) {
                empty = false;
                if (hasTranslucent(quad.getSprite())) {
                    return true;
                }
            }
        }
        return empty;
    }
    *///?}
}
