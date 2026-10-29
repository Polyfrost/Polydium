package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = /*? if >= 26.3 {*/"net.minecraft.world.level.levelgen.material.condition.VerticalGradientCondition$1"/*?} else {*//*"net.minecraft.world.level.levelgen.SurfaceRules$VerticalGradientConditionSource$1VerticalGradientCondition"*//*?}*/)
public abstract class MixinGradientRandomRoll {
    @Unique
    private static final RandomSource polydium$ROLLED = new XoroshiroRandomSource(0L, 0L);

    @Unique
    private float polydium$roll;

    @Redirect(method = "compute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;at(III)Lnet/minecraft/util/RandomSource;"))
    private RandomSource polydium$at(PositionalRandomFactory factory, int x, int y, int z) {
        if (!(factory instanceof XoroshiroRandomSource.XoroshiroPositionalRandomFactory)) {
            return factory.at(x, y, z);
        }
        XoroshiroPositionalFactoryAccessor seeds = (XoroshiroPositionalFactoryAccessor) factory;
        this.polydium$roll = polydium$firstFloat(Mth.getSeed(x, y, z) ^ seeds.polydium$seedLo(), seeds.polydium$seedHi());
        return polydium$ROLLED;
    }

    @Redirect(method = "compute", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F"))
    private float polydium$nextFloat(RandomSource random) {
        return random == polydium$ROLLED ? this.polydium$roll : random.nextFloat();
    }

    @Unique
    private static float polydium$firstFloat(long lo, long hi) {
        if ((lo | hi) == 0L) {
            lo = -7046029254386353131L;
            hi = 7640891576956012809L;
        }
        return (float) ((Long.rotateLeft(lo + hi, 17) + lo) >>> 40) * 5.9604645E-8F;
    }
}
