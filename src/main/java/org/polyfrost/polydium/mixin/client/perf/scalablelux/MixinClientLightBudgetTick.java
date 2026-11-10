package org.polyfrost.polydium.mixin.client.perf.scalablelux;

import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.perf.ClientLightBudget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinClientLightBudgetTick {
    @Inject(method = "tick", at = @At("HEAD"))
    private void polydium$markTickFrame(CallbackInfo ci) {
        ClientLightBudget.ticked = true;
    }
}
