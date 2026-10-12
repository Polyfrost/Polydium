package org.polyfrost.polydium.mixin.client.perf.iris;

import com.bawnorton.mixinsquared.TargetHandler;
import net.irisshaders.iris.Iris;
//? if >= 1.21.8 {
import net.minecraft.client.renderer.fog.FogRenderer;
//?} else {
/*import net.minecraft.client.renderer.FogRenderer;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FogRenderer.class, priority = 1500)
public class MixinFogRenderer {
    @TargetHandler(mixin = "net.irisshaders.iris.mixin.MixinFogRenderer", name = "iris$setupLegacyWaterFog")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private /*? if < 1.21.8 {*//*static *//*?}*/void polydium$skipWithoutPack(CallbackInfo ci) {
        if (!Iris.isPackInUseQuick()) ci.cancel();
    }
}
