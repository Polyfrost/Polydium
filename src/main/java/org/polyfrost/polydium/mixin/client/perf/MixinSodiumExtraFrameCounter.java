package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.polyfrost.polydium.client.perf.FrameStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "me.flashyreese.mods.sodiumextra.client.FrameCounter", remap = false)
public abstract class MixinSodiumExtraFrameCounter {
    @Unique private static final long IDLE_NANOS = 2_000_000_000L;

    @Shadow private double cachedSmoothFps;
    @Shadow private double cachedAverageFps;
    @Shadow private double cachedOnePercentLowFps;
    @Shadow private double cachedPointOnePercentLowFps;

    @Unique private final FrameStats polydium$stats = new FrameStats();
    @Unique private long polydium$lastRead = System.nanoTime() - IDLE_NANOS;
    @Unique private boolean polydium$readsSeen;

    @Inject(method = {"getSmoothFps", "getAverageFps", "getOnePercentLowFps", "getPointOnePercentLowFps"}, at = @At("HEAD"), remap = false, require = 0)
    private void polydium$markRead(CallbackInfoReturnable<Integer> cir) {
        this.polydium$lastRead = System.nanoTime();
        this.polydium$readsSeen = true;
    }

    @WrapMethod(method = "onFrame", remap = false, require = 0)
    private void polydium$sample(Operation<Void> original) {
        if (!this.polydium$readsSeen) {
            original.call();
            return;
        }

        long now = System.nanoTime();
        FrameStats stats = this.polydium$stats;
        if (now - this.polydium$lastRead >= IDLE_NANOS) {
            stats.pause();
            return;
        }

        if (stats.frame(now)) {
            this.cachedSmoothFps = stats.smooth;
            this.cachedAverageFps = stats.average;
            this.cachedOnePercentLowFps = stats.onePercentLow;
            this.cachedPointOnePercentLowFps = stats.pointOnePercentLow;
        }
    }
}
