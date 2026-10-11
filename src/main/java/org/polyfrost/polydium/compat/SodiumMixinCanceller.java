package org.polyfrost.polydium.compat;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;

import java.util.List;

public final class SodiumMixinCanceller implements MixinCanceller {
    private static final String SODIUM_ENTITY_CULL = "net.caffeinemc.mods.sodium.mixin.features.render.entity.cull.EntityRendererMixin";

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        //? if >= 1.21.11 {
        return PolydiumMixinPlugin.SODIUM_PERF && mixinClassName.equals(SODIUM_ENTITY_CULL)
                && !PolydiumMixinPlugin.isDisabled("MixinEntityCheckOrder");
        //?} else {
        /*return false;
        *///?}
    }
}
