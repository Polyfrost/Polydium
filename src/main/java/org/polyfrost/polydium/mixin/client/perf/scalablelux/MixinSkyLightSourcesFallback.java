package org.polyfrost.polydium.mixin.client.perf.scalablelux;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkAccess.class)
public abstract class MixinSkyLightSourcesFallback {
    @Shadow
    protected ChunkSkyLightSources skyLightSources;

    @Shadow
    @Final
    protected LevelHeightAccessor levelHeightAccessor;

    @Inject(method = "getSkyLightSources", at = @At("HEAD"))
    private void polydium$buildSkyLightSources(CallbackInfoReturnable<ChunkSkyLightSources> cir) {
        if (this.skyLightSources != null)
            return;
        synchronized (this) {
            if (this.skyLightSources != null)
                return;
            ChunkSkyLightSources sources = new ChunkSkyLightSources(this.levelHeightAccessor);
            sources.fillFrom((ChunkAccess) (Object) this);
            this.skyLightSources = sources;
        }
    }

    @Inject(method = "initializeLightSources", at = @At("TAIL"))
    private void polydium$refillSkyLightSources(CallbackInfo ci) {
        ChunkSkyLightSources sources = this.skyLightSources;
        if (sources != null)
            sources.fillFrom((ChunkAccess) (Object) this);
    }
}
