package org.polyfrost.polydium.mixin.client.perf.dh;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.polyfrost.polydium.client.perf.DhLodDepth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.common.render.openGl.glObject.shader.GlShader", remap = false)
public class MixinDhGlShader {
    @ModifyReturnValue(method = "loadFile", at = @At("RETURN"), require = 0)
    private static String polydium$dhGlFadeSource(String source, String path) {
        return DhLodDepth.patchFade(path, source);
    }
}
//?}
