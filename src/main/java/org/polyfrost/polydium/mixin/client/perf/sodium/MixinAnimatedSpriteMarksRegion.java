package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import org.polyfrost.polydium.client.perf.SpriteMarkVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
//? if >= 26.1 {
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion", remap = false)
//?} else {
/*@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSection", remap = false)
*///?}
public abstract class MixinAnimatedSpriteMarksRegion {
    //? if >= 26.1 {
    @Inject(method = {"setSectionRenderState", "clearSectionRenderState"}, at = @At("TAIL"))
    private void polydium$invalidateSpriteMarks(CallbackInfo ci) {
    //?} else {
    /*@Inject(method = {"setRenderState", "clearRenderState"}, at = @At("TAIL"))
    private void polydium$invalidateSpriteMarks(CallbackInfoReturnable<Boolean> cir) {
    *///?}
        SpriteMarkVersion.bump();
    }
}
//?}
