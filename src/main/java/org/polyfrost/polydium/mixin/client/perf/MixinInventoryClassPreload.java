package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.perf.InventoryClassPreload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinInventoryClassPreload {
    @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
    private void polydium$preloadInventoryClasses(CallbackInfo ci) {
        InventoryClassPreload.start();
    }
}
