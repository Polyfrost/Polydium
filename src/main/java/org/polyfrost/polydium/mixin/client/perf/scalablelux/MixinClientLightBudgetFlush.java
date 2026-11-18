package org.polyfrost.polydium.mixin.client.perf.scalablelux;

import net.minecraft.client.multiplayer.ClientLevel;
import org.polyfrost.polydium.client.perf.ClientLightBudget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinClientLightBudgetFlush {
    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"))
    private void polydium$flushLightForBreak(CallbackInfo ci) {
        ClientLightBudget.flush = true;
    }
}
