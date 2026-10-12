package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.ListenerTransform;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEngineExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class MixinSoundListenerThrottle {
    @Unique
    private static final long POLYDIUM$MIN_INTERVAL_NANOS = 5_000_000L;

    @Unique
    private ListenerTransform polydium$sentTransform;
    @Unique
    private long polydium$sentNanos;

    @WrapOperation(
        method = "updateSource",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngineExecutor;execute(Ljava/lang/Runnable;)V")
    )
    private void polydium$throttleListener(SoundEngineExecutor executor, Runnable task, Operation<Void> original, @Local ListenerTransform transform) {
        if (!PolydiumToggles.soundListenerThrottle) {
            this.polydium$sentTransform = null;
            original.call(executor, task);
            return;
        }
        ListenerTransform sent = this.polydium$sentTransform;
        if (sent != null && transform.position().equals(sent.position()) && transform.forward().equals(sent.forward()) && transform.up().equals(sent.up())) {
            return;
        }
        long now = System.nanoTime();
        if (sent != null && now - this.polydium$sentNanos < POLYDIUM$MIN_INTERVAL_NANOS) {
            return;
        }
        original.call(executor, task);
        this.polydium$sentTransform = transform;
        this.polydium$sentNanos = now;
    }

    @Inject(method = {"loadLibrary", "stopAll"}, at = @At("HEAD"))
    private void polydium$forgetSentListener(CallbackInfo ci) {
        this.polydium$sentTransform = null;
    }
}
//?}
