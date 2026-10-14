package org.polyfrost.polydium.mixin.client.perf.sodium.v08;

//? if >= 26.1 && < 26.2 {
/*import org.polyfrost.polydium.client.perf.SpriteMarkVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSection", remap = false)
public abstract class MixinAnimatedSpriteMarksRegion {
    @Inject(method = {"setRenderState", "clearRenderState"}, at = @At("TAIL"))
    private void polydium$invalidateSpriteMarks(CallbackInfoReturnable<Boolean> cir) {
        SpriteMarkVersion.bump();
    }
}
*///?}
