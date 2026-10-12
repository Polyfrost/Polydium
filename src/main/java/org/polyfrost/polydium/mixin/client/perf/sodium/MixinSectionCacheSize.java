package org.polyfrost.polydium.mixin.client.perf.sodium;

import org.polyfrost.polydium.client.PolydiumToggles;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.polyfrost.polydium.client.perf.SectionStates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache", remap = false)
public abstract class MixinSectionCacheSize {
    @ModifyExpressionValue(method = "acquire", at = @At(value = "CONSTANT", args = "intValue=512"), require = 0)
    private int polydium$cacheSize(int size) {
        return PolydiumToggles.sectionCacheSize ? Math.max(size, SectionStates.CACHE_SIZE) : size;
    }
}
