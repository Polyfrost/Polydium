package org.polyfrost.polydium.mixin.client.perf.scalablelux;

import net.minecraft.world.level.chunk.DataLayer;
import org.polyfrost.polydium.client.perf.LightNibbleView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "ca.spottedleaf.starlight.common.light.SWMRNibbleArray", remap = false)
public abstract class MixinLightNibbleView {
    @Shadow
    protected volatile int stateVisible;
    @Shadow
    protected volatile byte[] storageVisible;

    @Inject(method = "toVanillaNibble", at = @At("HEAD"), cancellable = true, require = 0)
    private void polydium$view(CallbackInfoReturnable<DataLayer> cir) {
        if (LightNibbleView.viewer == Thread.currentThread()) {
            synchronized (this) {
                if (this.stateVisible == 2) {
                    cir.setReturnValue(new DataLayer(this.storageVisible));
                }
            }
        }
    }
}
