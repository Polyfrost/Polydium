package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.world.level.biome.Climate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.level.biome.Climate$RTree")
public abstract class MixinClimateSearchScratch {
    @Unique
    private static final ThreadLocal<long[]> polydium$target = ThreadLocal.withInitial(() -> new long[7]);

    @Redirect(method = "search", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Climate$TargetPoint;toParameterArray()[J"))
    private long[] polydium$scratchTarget(Climate.TargetPoint target) {
        long[] array = polydium$target.get();
        array[0] = target.temperature();
        array[1] = target.humidity();
        array[2] = target.continentalness();
        array[3] = target.erosion();
        array[4] = target.depth();
        array[5] = target.weirdness();
        return array;
    }
}
