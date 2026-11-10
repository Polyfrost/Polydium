package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.polyfrost.polydium.client.perf.IdleBlockEntityTicks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
public abstract class MixinIdleBlockEntityTick {
    @Shadow
    @Final
    private BlockEntity blockEntity;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void polydium$skipIdleTick(CallbackInfo ci) {
        if (IdleBlockEntityTicks.isIdle(this.blockEntity))
            ci.cancel();
    }
}
