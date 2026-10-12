package org.polyfrost.polydium.mixin.client.perf.sodium;

import net.minecraft.client.renderer.texture.SpriteContents;
import org.polyfrost.polydium.client.perf.SpriteMarkVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >= 1.21.11 {
@Mixin(SpriteContents.AnimationState.class)
//?} else {
/*@Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$Ticker")
*///?}
public abstract class MixinAnimatedSpriteMarksAnimationState {
    @Inject(method = /*? if >= 1.21.11 {*/"tick"/*?} else {*//*"tickAndUpload"*//*?}*/, at = @At("HEAD"))
    private void polydium$invalidateSpriteMarks(CallbackInfo ci) {
        SpriteMarkVersion.bump();
    }
}
