package org.polyfrost.polydium.mixin.client.perf.dh;

//? if >= 1.21.10 {
import org.polyfrost.polydium.client.perf.DhLodDepth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.common.render.openGl.GlDhMetaRenderer", remap = false)
public class MixinDhGlMetaRenderer {
    @Inject(method = "copyToMcTexture", at = @At("RETURN"), require = 0)
    private void polydium$dhGlLodsApplied(CallbackInfo ci) {
        DhLodDepth.lodsApplied();
    }
}
//?}
