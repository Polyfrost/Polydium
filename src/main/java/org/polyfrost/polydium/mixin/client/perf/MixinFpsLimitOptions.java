package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
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

    @ModifyConstant(method = {"lambda$new$4", "lambda$new$5"}, constant = @Constant(intValue = 260))
    private static int polydium$unlimitedLabel(int original) {
        return 1500;
    }
}
//?}
