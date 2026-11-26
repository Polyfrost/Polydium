package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogData;
import org.polyfrost.polydium.compat.BobbySkyFog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class, priority = 1500)
public abstract class MixinBobbySkyFogUpdate {
    private static final String POLYDIUM$MIXIN = "de.johni0702.minecraft.bobby.mixin.GameRendererMixin";

    @TargetHandler(mixin = POLYDIUM$MIXIN, name = "updateSkyFogRenderer")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), require = 0, expect = 0)
    private void polydium$resetSkyFogUpdated(CallbackInfoReturnable<FogData> cir) {
        BobbySkyFog.updated = false;
    }

    @TargetHandler(mixin = POLYDIUM$MIXIN, name = "updateSkyFogRenderer")
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V", shift = At.Shift.AFTER), require = 0, expect = 0)
    private void polydium$markSkyFogUpdated(CallbackInfoReturnable<FogData> cir) {
        BobbySkyFog.updated = true;
    }
}
//?}
