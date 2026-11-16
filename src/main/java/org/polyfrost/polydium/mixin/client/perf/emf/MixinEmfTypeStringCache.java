package org.polyfrost.polydium.mixin.client.perf.emf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "traben.entity_model_features.models.animation.state.EMFEntityRenderStateViaReference", remap = false)
public abstract class MixinEmfTypeStringCache {
    @Unique
    private String polydium$typeString;

    @Shadow
    public abstract String typeString();

    @Inject(method = "<init>", at = @At("RETURN"), remap = false, require = 0)
    private void polydium$readTypeString(@Coerce Object entity, CallbackInfo ci) {
        if (entity != null)
            typeString();
    }

    @WrapMethod(method = "typeString", remap = false, require = 0)
    private String polydium$inline$cachedTypeString(Operation<String> original) {
        String type = polydium$typeString;
        if (type == null) {
            type = original.call();
            polydium$typeString = type;
        }
        return type;
    }
}
