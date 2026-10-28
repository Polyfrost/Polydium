package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.server.Bootstrap;
import org.polyfrost.polydium.client.perf.BlockStateCacheBatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public abstract class MixinBootstrapParallelCache {
    @Inject(method = "bootStrap", at = @At(value = "FIELD", target = "Lnet/minecraft/server/Bootstrap;isBootstrapped:Z", opcode = org.objectweb.asm.Opcodes.PUTSTATIC))
    private static void polydium$startCollecting(CallbackInfo ci) {
        BlockStateCacheBatch.collecting = true;
    }

    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FireBlock;bootStrap()V", shift = At.Shift.AFTER))
    private static void polydium$buildCachesInParallel(CallbackInfo ci) {
        BlockStateCacheBatch.buildAll();
    }
}
