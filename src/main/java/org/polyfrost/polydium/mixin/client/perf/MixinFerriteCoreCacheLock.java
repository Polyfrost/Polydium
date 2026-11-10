package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "malte0811.ferritecore.impl.BlockStateCacheImpl", remap = false)
public abstract class MixinFerriteCoreCacheLock {
    @Unique
    private static final Object polydium$LOCK = new Object();

    @WrapMethod(method = "deduplicateCachePost")
    private static void polydium$lock(BlockBehaviour.BlockStateBase state, Operation<Void> original) {
        synchronized (polydium$LOCK) {
            original.call(state);
        }
    }
}
