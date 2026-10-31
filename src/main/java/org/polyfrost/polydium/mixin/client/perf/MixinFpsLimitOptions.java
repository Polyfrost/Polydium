package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Options.class)
public abstract class MixinFpsLimitOptions {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 26), slice = @Slice(
        from = @At(value = "CONSTANT", args = "stringValue=options.framerateLimit"),
        to = @At(value = "CONSTANT", args = "intValue=260")))
    private int polydium$sliderSteps(int original) {
        return 150;
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 260))
    private int polydium$codecMax(int original) {
        return 1500;
    }

    // A stored 260 is vanilla's unlimited, so a real 260 cap is stored as 261 to stay distinct.
    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;intRange(II)Lcom/mojang/serialization/Codec;", ordinal = 0), slice = @Slice(
        from = @At(value = "CONSTANT", args = "stringValue=options.framerateLimit")))
    private Codec<Integer> polydium$readVanillaUnlimited(Codec<Integer> original) {
        return original.xmap(
            value -> value == 260 ? 1500 : value == 261 ? 260 : value,
            value -> value == 260 ? 261 : value);
    }

    //? if >= 26.1 {
    @ModifyConstant(method = {"lambda$new$4", "lambda$new$5"}, constant = @Constant(intValue = 260))
    //?} else {
    /*@ModifyConstant(method = "method_42559", constant = @Constant(intValue = 260))
    *///?}
    private static int polydium$unlimitedLabel(int original) {
        return 1500;
    }
}
