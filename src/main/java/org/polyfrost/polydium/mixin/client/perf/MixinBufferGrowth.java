package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import org.polyfrost.polydium.client.perf.BufferGrowth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ByteBufferBuilder.class)
public abstract class MixinBufferGrowth {
    //? if >= 1.21.8 {
    @Shadow
    private long capacity;

    @ModifyExpressionValue(method = "ensureCapacity", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(JJ)J"))
    private long polydium$geometricGrowth(long growth) {
        return BufferGrowth.growth(this.capacity, growth);
    }
    //?} else {
    /*@Shadow
    private int capacity;

    @ModifyExpressionValue(method = "ensureCapacity", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int polydium$geometricGrowth(int growth) {
        return BufferGrowth.growth(this.capacity, growth);
    }
    *///?}
}
