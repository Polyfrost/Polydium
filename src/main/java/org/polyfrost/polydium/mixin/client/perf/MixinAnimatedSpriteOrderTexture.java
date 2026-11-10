package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.polyfrost.polydium.client.perf.AnimatedSpriteOrder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$AnimatedTexture")
public abstract class MixinAnimatedSpriteOrderTexture {
    @Shadow
    @Final
    boolean interpolateFrames;

    @ModifyReturnValue(method = "createAnimationState", at = @At("RETURN"))
    private SpriteContents.AnimationState polydium$markInterpolated(SpriteContents.AnimationState state) {
        if (state != null)
            ((AnimatedSpriteOrder.Interpolated) state).polydium$interpolateFrames(this.interpolateFrames);

        return state;
    }
}
//?}
