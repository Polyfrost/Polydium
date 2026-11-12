package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import net.caffeinemc.mods.sodium.client.render.chunk.async.CullTask;
import org.polyfrost.polydium.client.perf.SodiumCullTreePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(value = CullTask.class, remap = false)
public abstract class MixinCullTaskTreePool implements SodiumCullTreePool.Owner {
    @Unique
    private final ArrayList<long[]> polydium$treeArrays = new ArrayList<>();

    @Override
    public ArrayList<long[]> polydium$treeArrays() {
        return this.polydium$treeArrays;
    }

    @Inject(method = "runTask", at = @At("HEAD"))
    private void polydium$collectTrees(CallbackInfoReturnable<?> cir) {
        SodiumCullTreePool.begin(this.polydium$treeArrays);
    }

    @Inject(method = "runTask", at = @At("RETURN"))
    private void polydium$stopCollecting(CallbackInfoReturnable<?> cir) {
        SodiumCullTreePool.end();
    }
}
//?}
