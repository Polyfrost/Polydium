package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 && < 26.2 {
/*import net.minecraft.client.renderer.GameRenderer;
import org.polyfrost.polydium.client.perf.ImmediateDrawRing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinImmediateDrawRingClose {
    @Inject(method = "close", at = @At("HEAD"))
    private void polydium$closeImmediateRing(CallbackInfo ci) {
        ImmediateDrawRing.close();
    }
}
*///?}
