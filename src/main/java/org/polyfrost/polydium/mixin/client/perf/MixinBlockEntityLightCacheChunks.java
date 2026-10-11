package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import org.polyfrost.polydium.client.perf.BlockEntityLight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkCache.class)
public class MixinBlockEntityLightCacheChunks {
    @Inject(method = "onLightUpdate", at = @At("HEAD"))
    private void polydium$lightChanged(LightLayer layer, SectionPos pos, CallbackInfo ci) {
        BlockEntityLight.invalidateAround(pos.getX(), pos.getY(), pos.getZ());
    }

    @Inject(method = "drop", at = @At("HEAD"))
    private void polydium$forgetChunk(ChunkPos pos, CallbackInfo ci) {
        BlockEntityLight.forgetChunk(pos.getMinBlockX() >> 4, pos.getMinBlockZ() >> 4);
    }
}
