package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import net.minecraft.client.renderer.extract.LevelExtractor;
//?} else {
/*import net.minecraft.client.renderer.LevelRenderer;
*///?}
import org.polyfrost.polydium.client.perf.CutoutTypeCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(/*? if >= 26.2 {*/LevelExtractor/*?} else {*//*LevelRenderer*//*?}*/.class)
public abstract class MixinCutoutTypeCacheClear {
    @Inject(method = "allChanged", at = @At("HEAD"))
    private void polydium$clearCutoutTypes(CallbackInfo ci) {
        CutoutTypeCache.clear();
    }
}
