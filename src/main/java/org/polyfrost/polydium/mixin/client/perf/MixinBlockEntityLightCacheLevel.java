package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.multiplayer.ClientLevel;
import org.polyfrost.polydium.client.perf.BlockEntityLight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MixinBlockEntityLightCacheLevel {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$newLevel(CallbackInfo ci) {
        BlockEntityLight.clear();
    }

    @Inject(method = "setSectionDirtyWithNeighbors", at = @At("HEAD"))
    private void polydium$lightDataReceived(int sectionX, int sectionY, int sectionZ, CallbackInfo ci) {
        BlockEntityLight.invalidateAround(sectionX, sectionY, sectionZ);
    }

    //? if >= 1.21.4 {
    @Inject(method = "setSectionRangeDirty", at = @At("HEAD"))
    private void polydium$chunkLightEnabled(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, CallbackInfo ci) {
        BlockEntityLight.invalidate(minX, minY, minZ, maxX, maxY, maxZ);
    }
    //?}
}
