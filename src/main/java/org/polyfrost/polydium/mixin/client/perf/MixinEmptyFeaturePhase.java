package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleFeatureRenderPhase.class)
public class MixinEmptyFeaturePhase {
    @Unique
    private boolean polydium$hasSubmits;

    @ModifyVariable(method = "submit", at = @At("HEAD"), argsOnly = true)
    private SubmitNode polydium$markSubmitted(SubmitNode submit) {
        this.polydium$hasSubmits = true;
        return submit;
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void polydium$markCleared(CallbackInfo ci) {
        this.polydium$hasSubmits = false;
    }

    @WrapMethod(method = "isEmpty")
    private boolean polydium$emptyWithoutWalking(Operation<Boolean> original) {
        if (!this.polydium$hasSubmits) {
            return true;
        }
        return original.call();
    }
}
//?}
