package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 && < 26.2 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import org.polyfrost.polydium.client.perf.ImmediateDrawRing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public abstract class MixinImmediateDrawRingFrame {
    @Inject(method = "flipFrame", at = @At("HEAD"), order = 900)
    private static void polydium$endImmediateFrame(CallbackInfo ci) {
        ImmediateDrawRing.endFrame();
    }
}
*///?}
