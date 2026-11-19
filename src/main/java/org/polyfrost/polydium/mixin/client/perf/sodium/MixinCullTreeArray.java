package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.Tree;
import org.polyfrost.polydium.client.perf.SodiumCullTreePool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Tree.class, remap = false)
public abstract class MixinCullTreeArray {
    @Shadow
    @Final
    @Mutable
    protected long[] tree;

    @ModifyExpressionValue(method = "<init>", at = @At(value = "CONSTANT", args = "intValue=4096"))
    private int polydium$skipFreshArray(int size) {
        return SodiumCullTreePool.collecting() ? 0 : size;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$pooledArray(CallbackInfo ci) {
        if (this.tree.length == 0) {
            this.tree = SodiumCullTreePool.take();
        }
    }
}
//?}
