package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;
import org.polyfrost.polydium.client.perf.ParticleAirCollision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientChunkCache.class)
public abstract class MixinParticleCollisionReachViewRadius {
    @ModifyExpressionValue(method = "updateViewRadius", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;inRange(II)Z"))
    private boolean polydium$detachDropped(boolean inRange, @Local LevelChunk chunk) {
        if (!inRange)
            ParticleAirCollision.detach(chunk);
        return inRange;
    }
}
//?}
