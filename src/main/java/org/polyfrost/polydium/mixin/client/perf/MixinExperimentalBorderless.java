package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Window.class)
public class MixinExperimentalBorderless {
    @ModifyReturnValue(method = "useBorderlessFullscreenWindow", at = @At("RETURN"))
    private boolean polydium$exactBorderless(boolean original) {
        return original && !PolydiumToggles.experimentalBorderless;
    }
}
//?}
