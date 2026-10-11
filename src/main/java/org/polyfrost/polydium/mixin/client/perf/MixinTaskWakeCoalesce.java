package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(BlockableEventLoop.class)
public abstract class MixinTaskWakeCoalesce {
    @Unique
    private final AtomicBoolean polydium$wakePending = new AtomicBoolean();

    @WrapWithCondition(
            method = /*? if >= 1.21.4 {*/"schedule"/*?} else {*//*"tell"*//*?}*/,
            at = @At(value = "INVOKE", target = "Ljava/util/concurrent/locks/LockSupport;unpark(Ljava/lang/Thread;)V")
    )
    private boolean polydium$wakeOnce(Thread thread) {
        return !((Object) this instanceof Minecraft) || this.polydium$wakePending.compareAndSet(false, true);
    }

    @Inject(method = "pollTask", at = @At("HEAD"))
    private void polydium$rearmWake(CallbackInfoReturnable<Boolean> cir) {
        if (this.polydium$wakePending.get()) {
            this.polydium$wakePending.set(false);
        }
    }
}
