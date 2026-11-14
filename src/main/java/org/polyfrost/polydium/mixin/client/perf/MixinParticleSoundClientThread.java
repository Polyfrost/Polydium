package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
//? if < 1.21.5
/*import net.minecraft.world.entity.player.Player;*/
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientLevel.class, priority = 1100)
public abstract class MixinParticleSoundClientThread {
    @Unique
    private boolean polydium$handOff(Runnable call) {
        Minecraft client = Minecraft.getInstance();
        if (client.isSameThread())
            return false;
        client.execute(() -> {
            if (client.level == (Object) this)
                call.run();
        });
        return true;
    }

    @Inject(method = "playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V", at = @At("HEAD"), cancellable = true)
    private void polydium$playOnClientThread(double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, boolean distanceDelay, CallbackInfo ci) {
        if (polydium$handOff(() -> ((ClientLevel) (Object) this).playLocalSound(x, y, z, sound, source, volume, pitch, distanceDelay)))
            ci.cancel();
    }

    @Inject(method = "playLocalSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", at = @At("HEAD"), cancellable = true)
    private void polydium$playEntityOnClientThread(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch, CallbackInfo ci) {
        if (polydium$handOff(() -> ((ClientLevel) (Object) this).playLocalSound(entity, sound, source, volume, pitch)))
            ci.cancel();
    }

    //? if >= 1.21.5 {
    @Inject(method = "playPlayerSound", at = @At("HEAD"), cancellable = true)
    private void polydium$playPlayerOnClientThread(SoundEvent sound, SoundSource source, float volume, float pitch, CallbackInfo ci) {
        if (polydium$handOff(() -> ((ClientLevel) (Object) this).playPlayerSound(sound, source, volume, pitch)))
            ci.cancel();
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void polydium$playSeededEntityOnClientThread(Entity except, Entity entity, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void polydium$playSeededEntityOnClientThread(Player except, Entity entity, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed, CallbackInfo ci) {
    *///?}
        if (polydium$handOff(() -> ((ClientLevel) (Object) this).playSeededSound(except, entity, sound, source, volume, pitch, seed)))
            ci.cancel();
    }

    @Inject(method = "playSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZJ)V", at = @At("HEAD"), cancellable = true)
    private void polydium$playSeededOnClientThread(double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, boolean distanceDelay, long seed, CallbackInfo ci) {
        if (polydium$handOff(() -> this.playSound(x, y, z, sound, source, volume, pitch, distanceDelay, seed)))
            ci.cancel();
    }

    @Shadow
    private void playSound(double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, boolean distanceDelay, long seed) {
        throw new AssertionError();
    }

    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void polydium$addOnClientThread(ParticleOptions particle, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
        if (polydium$handOff(() -> ((ClientLevel) (Object) this).addParticle(particle, x, y, z, xd, yd, zd)))
            ci.cancel();
    }

    //? if >= 1.21.4 {
    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void polydium$addFlagsOnClientThread(ParticleOptions particle, boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
        if (polydium$handOff(() -> ((ClientLevel) (Object) this).addParticle(particle, overrideLimiter, alwaysShow, x, y, z, xd, yd, zd)))
            ci.cancel();
    }
    //?} else {
    /*@Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void polydium$addFlagOnClientThread(ParticleOptions particle, boolean overrideLimiter, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
        if (polydium$handOff(() -> ((ClientLevel) (Object) this).addParticle(particle, overrideLimiter, x, y, z, xd, yd, zd)))
            ci.cancel();
    }
    *///?}

    @Inject(method = "addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void polydium$addAlwaysVisibleOnClientThread(ParticleOptions particle, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
        if (polydium$handOff(() -> ((ClientLevel) (Object) this).addAlwaysVisibleParticle(particle, x, y, z, xd, yd, zd)))
            ci.cancel();
    }

    @Inject(method = "addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void polydium$addAlwaysVisibleFlagOnClientThread(ParticleOptions particle, boolean overrideLimiter, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
        if (polydium$handOff(() -> ((ClientLevel) (Object) this).addAlwaysVisibleParticle(particle, overrideLimiter, x, y, z, xd, yd, zd)))
            ci.cancel();
    }
}
