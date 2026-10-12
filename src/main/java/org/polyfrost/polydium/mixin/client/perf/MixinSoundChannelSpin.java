package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(SoundEngine.class)
public abstract class MixinSoundChannelSpin {
    @Unique
    private static final long POLYDIUM$MAX_SPIN_NANOS = 200_000L;

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;join()Ljava/lang/Object;"))
    private Object polydium$spinForChannel(CompletableFuture<?> channel, Operation<Object> original) {
        if (!channel.isDone()) {
            long deadline = System.nanoTime() + POLYDIUM$MAX_SPIN_NANOS;
            while (!channel.isDone() && System.nanoTime() - deadline < 0L) {
                Thread.onSpinWait();
            }
        }
        return original.call(channel);
    }
}
//?}
