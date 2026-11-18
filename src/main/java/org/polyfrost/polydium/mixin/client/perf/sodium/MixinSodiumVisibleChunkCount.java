package org.polyfrost.polydium.mixin.client.perf.sodium;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class MixinSodiumVisibleChunkCount {
    @Shadow
    private RenderSectionManager renderSectionManager;

    @Inject(method = "getVisibleChunkCount", at = @At("HEAD"), cancellable = true)
    private void polydium$noLevelNoSections(CallbackInfoReturnable<Integer> cir) {
        if (this.renderSectionManager == null)
            cir.setReturnValue(0);
    }
}
