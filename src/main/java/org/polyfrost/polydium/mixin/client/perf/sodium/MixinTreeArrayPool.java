package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import org.polyfrost.polydium.client.perf.TreeArrayPool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.tree.Tree", remap = false)
public abstract class MixinTreeArrayPool {
    @Shadow @Final @Mutable
    protected long[] tree;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = TreeArrayPool.TREE_LONGS))
    private int polydium$skipTreeAllocation(int length) {
        return 0;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$usePooledTree(CallbackInfo ci) {
        if (this.tree.length == 0) {
            this.tree = TreeArrayPool.acquire();
        }
    }
}
//?}
