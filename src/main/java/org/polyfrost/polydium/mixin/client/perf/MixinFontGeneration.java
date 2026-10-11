package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.gui.font.FontManager;
import org.polyfrost.polydium.client.perf.FontGeneration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FontManager.class)
public abstract class MixinFontGeneration {
    @Inject(method = {"apply", "updateOptions"}, at = @At("TAIL"))
    private void polydium$bumpFontGeneration(CallbackInfo ci) {
        FontGeneration.bump();
    }
}
