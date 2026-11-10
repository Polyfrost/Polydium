package org.polyfrost.polydium.mixin.client.perf.voxy;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "me.cortex.voxy.client.core.gl.shader.ShaderLoader", remap = false)
public class MixinVoxyShaderLoader {
    @ModifyReturnValue(method = "parse", at = @At("RETURN"), require = 0)
    private static String polydium$voxyFarClamp(String source, String id) {
        return SkyFarPlane.patchVoxy(id, source);
    }
}
//?}
