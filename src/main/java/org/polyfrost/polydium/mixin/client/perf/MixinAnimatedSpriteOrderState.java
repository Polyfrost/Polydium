package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import net.minecraft.client.renderer.texture.SpriteContents;
import org.polyfrost.polydium.client.perf.AnimatedSpriteOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpriteContents.AnimationState.class)
public abstract class MixinAnimatedSpriteOrderState implements AnimatedSpriteOrder.Interpolated {
    @Unique
    private boolean polydium$interpolateFrames;

    @Override
    public boolean polydium$interpolateFrames() {
        return this.polydium$interpolateFrames;
    }

    @Override
    public void polydium$interpolateFrames(boolean interpolate) {
        this.polydium$interpolateFrames = interpolate;
    }
}
//?}
