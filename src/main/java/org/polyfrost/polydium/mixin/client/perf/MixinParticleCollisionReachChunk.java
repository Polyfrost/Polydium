package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.polyfrost.polydium.client.perf.ParticleAirCollision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinParticleCollisionReachChunk {
    @Inject(method = "unload", at = @At("HEAD"))
    private void polydium$invalidateSections(LevelChunk chunk, CallbackInfo ci) {
        ParticleAirCollision.detach(chunk);
    }
}
//?}
