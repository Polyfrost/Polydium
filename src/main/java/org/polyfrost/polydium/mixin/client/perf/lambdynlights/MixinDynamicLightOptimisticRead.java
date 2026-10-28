package org.polyfrost.polydium.mixin.client.perf.lambdynlights;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

@Pseudo
@Mixin(targets = "dev.lambdaurora.lambdynlights.LambDynLights", remap = false)
public abstract class MixinDynamicLightOptimisticRead {
    @Unique
    private final StampedLock polydium$sourcesStamp = new StampedLock();
    @Unique
    private long polydium$writeStamp;
    @Unique
    private volatile boolean polydium$writesStamped;

    @Shadow(remap = false)
    public abstract double getDynamicLightLevel(BlockPos pos);

    @Shadow(remap = false)
    public abstract int getLightmapWithDynamicLight(double dynamicLightLevel, int lightmap);

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;lock()V"), remap = false, require = 0)
    private void polydium$stampWrite(ReentrantReadWriteLock.WriteLock lock) {
        lock.lock();
        if (lock.getHoldCount() == 1) {
            polydium$writeStamp = polydium$sourcesStamp.writeLock();
            polydium$writesStamped = true;
        }
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;unlock()V"), remap = false, require = 0)
    private void polydium$unstampWrite(ReentrantReadWriteLock.WriteLock lock) {
        long stamp = polydium$writeStamp;
        if (stamp != 0 && lock.getHoldCount() == 1) {
            polydium$writeStamp = 0;
            polydium$sourcesStamp.unlockWrite(stamp);
        }
        lock.unlock();
    }

    @WrapMethod(
            method = /*? if >= 26.1 {*/"getLightmapWithDynamicLight(Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/core/BlockPos;I)I"/*?} else {*//*"getLightmapWithDynamicLight(Lnet/minecraft/class_1920;Lnet/minecraft/class_2338;I)I"*//*?}*/,
            remap = false, require = 0)
    private int polydium$inline$optimisticLightmap(
            //? if >= 26.1 {
            net.minecraft.world.level.BlockAndLightGetter level,
            //?} else {
            /*net.minecraft.world.level.BlockAndTintGetter level,
            *///?}
            BlockPos pos, int lightmap, Operation<Integer> original
    ) {
        if (PolydiumToggles.dynamicLightOptimisticRead && polydium$writesStamped) {
            long stamp = polydium$sourcesStamp.tryOptimisticRead();
            if (stamp != 0) {
                try {
                    double dynamicLevel = getDynamicLightLevel(pos);
                    if (polydium$sourcesStamp.validate(stamp))
                        return getLightmapWithDynamicLight(dynamicLevel, lightmap);
                } catch (RuntimeException ignored) {
                }
            }
        }
        return original.call(level, pos, lightmap);
    }
}
