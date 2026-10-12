package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 {
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.perf.OcclusionCuller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinOcclusionReset {
    @Inject(method = "setLevel", at = @At("TAIL"))
    private void polydium$resetOcclusion(CallbackInfo ci) {
        OcclusionCuller.reset();
    }
}
//?}
