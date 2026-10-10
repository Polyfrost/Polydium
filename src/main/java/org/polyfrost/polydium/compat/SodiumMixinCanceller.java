package org.polyfrost.polydium.compat;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public final class SodiumMixinCanceller implements MixinCanceller {
    private static final String SODIUM_ENTITY_CULL = "net.caffeinemc.mods.sodium.mixin.features.render.entity.cull.EntityRendererMixin";
    private static final boolean SODIUM_PERF = FabricLoader.getInstance().getModContainer("sodium")
            .map(mod -> mod.getMetadata().getVersion().getFriendlyString().startsWith(/*? if >= 26.1 {*/"0.9.2"/*?} else {*//*"0.8.14"*//*?}*/))
            .orElse(false);

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        //? if >= 1.21.11 {
        return SODIUM_PERF && mixinClassName.equals(SODIUM_ENTITY_CULL);
        //?} else {
        /*return false;
        *///?}
    }
}
