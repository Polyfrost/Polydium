package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.polyfrost.polydium.client.perf.PrecipitationCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkCache.class)
public class MixinPrecipitationCacheInvalidation {
    @Shadow
    @Final
    private ClientLevel level;

    @Inject(method = {"drop", "replaceBiomes", "updateViewRadius", "updateViewCenter"}, at = @At("RETURN"))
    private void polydium$clearPrecipitation(CallbackInfo ci) {
        ((PrecipitationCache.Access) this.level).polydium$precipitationCache().clear();
    }

    @Inject(method = "replaceWithPacketData", at = @At("RETURN"))
    private void polydium$clearPrecipitationOnLoad(CallbackInfoReturnable<LevelChunk> cir) {
        ((PrecipitationCache.Access) this.level).polydium$precipitationCache().clear();
    }
}
//?}
