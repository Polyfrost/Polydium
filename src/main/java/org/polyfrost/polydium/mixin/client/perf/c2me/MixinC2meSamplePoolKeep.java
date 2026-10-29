package org.polyfrost.polydium.mixin.client.perf.c2me;

//? if >= 26.3 {
import java.lang.ref.SoftReference;
import net.minecraft.world.level.levelgen.densityfunction.DensityBufferPool;
import org.polyfrost.polydium.client.perf.DensityPoolContexts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.ishland.c2me.opts.allocs.common.ObjectCachingUtils", remap = false)
public abstract class MixinC2meSamplePoolKeep {
    @Unique
    private static final ThreadLocal<SoftReference<DensityBufferPool>> polydium$kept = new ThreadLocal<>();

    @Inject(method = "returnCachedSampleBufferPool", at = @At("HEAD"), remap = false, require = 0)
    private static void polydium$releaseCaches(DensityBufferPool pool, CallbackInfo ci) {
        ((DensityPoolContexts) pool).polydium$closeContexts();
        pool.garbageCollect();
    }

    @Inject(method = "returnCachedSampleBufferPool", at = @At("TAIL"), remap = false, require = 0)
    private static void polydium$keepPool(DensityBufferPool pool, CallbackInfo ci) {
        SoftReference<DensityBufferPool> kept = polydium$kept.get();
        if (kept == null || kept.get() != pool) {
            polydium$kept.set(new SoftReference<>(pool));
        }
    }
}
//?}
