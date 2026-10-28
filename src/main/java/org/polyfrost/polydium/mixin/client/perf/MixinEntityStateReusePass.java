package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
//? if >= 26.2 {
import net.minecraft.client.renderer.extract.LevelExtractor;
//?} else
/*import net.minecraft.client.renderer.LevelRenderer;*/
import org.polyfrost.polydium.client.perf.EntityStateReuse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(/*? if >= 26.2 {*/LevelExtractor.class/*?} else {*//*LevelRenderer.class*//*?}*/)
public class MixinEntityStateReusePass {
    @Inject(method = "extractVisibleEntities", at = @At("HEAD"))
    private void polydium$beginStateReuse(CallbackInfo ci) {
        EntityStateReuse.begin();
    }

    @Inject(method = "extractVisibleEntities", at = @At("RETURN"))
    private void polydium$endStateReuse(CallbackInfo ci) {
        EntityStateReuse.end();
    }
}
//?}
