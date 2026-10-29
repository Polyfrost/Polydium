package org.polyfrost.polydium.mixin.client.perf.particle_core;

//? if >= 1.21.10 {
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.perf.ParticleTickOverlap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinParticleTickOverlapFrame {
    @Inject(method = "runTick", at = @At("HEAD"))
    private void polydium$finishPendingTick(CallbackInfo ci) {
        ParticleTickOverlap.frameStart();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void polydium$finishBeforeTick(CallbackInfo ci) {
        ParticleTickOverlap.complete();
    }
}
//?}
