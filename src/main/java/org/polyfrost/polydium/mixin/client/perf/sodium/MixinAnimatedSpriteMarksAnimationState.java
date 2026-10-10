package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import net.minecraft.client.renderer.texture.SpriteContents;
import org.polyfrost.polydium.client.perf.SpriteMarkVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.AnimationState.class)
public abstract class MixinAnimatedSpriteMarksAnimationState {
    @Inject(method = "tick", at = @At("HEAD"))
    private void polydium$invalidateSpriteMarks(CallbackInfo ci) {
        SpriteMarkVersion.bump();
    }
}
//?}
