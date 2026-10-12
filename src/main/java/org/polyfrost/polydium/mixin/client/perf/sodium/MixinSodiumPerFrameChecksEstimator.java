package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 || < 1.21.4 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.estimation.Estimator", remap = false)
public abstract class MixinSodiumPerFrameChecksEstimator {
    @Unique
    private boolean polydium$hasNewData;

    @Inject(method = "addData", at = @At("HEAD"))
    private void polydium$markNewData(CallbackInfo ci) {
        this.polydium$hasNewData = true;
    }

    @WrapMethod(method = "updateModels")
    private void polydium$skipIdleUpdate(Operation<Void> original) {
        if (!this.polydium$hasNewData) {
            return;
        }
        this.polydium$hasNewData = false;
        original.call();
    }
}
//?}
