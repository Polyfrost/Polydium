package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.DensityBufferArena;
import net.minecraft.world.level.levelgen.densityfunction.DensityBufferPool;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.polyfrost.polydium.client.perf.DensityPoolContexts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SamplerContext.class, RandomState.class})
public abstract class MixinSamplerContextCacheRelease {
    @Inject(method = "<init>(Lnet/minecraft/util/context/ContextMap;Lnet/minecraft/world/level/levelgen/densityfunction/DensityBufferArena;Z)V", at = @At("TAIL"), require = 0)
    private void polydium$track(ContextMap userFields, DensityBufferArena bufferArena, boolean enableCaches, CallbackInfo ci) {
        if (enableCaches && bufferArena instanceof DensityBufferPool pool) {
            ((DensityPoolContexts) pool).polydium$track((SamplerContext) (Object) this);
        }
    }

    @Inject(method = "releaseDensityBufferPool", at = @At("HEAD"), require = 0)
    private void polydium$release(DensityBufferPool pool, CallbackInfo ci) {
        ((DensityPoolContexts) pool).polydium$closeContexts();
    }
}
//?}
