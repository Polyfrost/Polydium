package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.compat.FontrastCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.isxander.yacl3.config.v2.impl.ConfigClassHandlerImpl", remap = false)
public abstract class MixinFontrastConfig {
    @Shadow(remap = false)
    public abstract Class<?> configClass();

    @Shadow(remap = false)
    public abstract Object instance();

    @Inject(method = "load", at = @At("RETURN"), remap = false, require = 0)
    private void polydium$fontrastLoaded(CallbackInfoReturnable<Boolean> cir) {
        this.polydium$fontrastChanged();
    }

    @Inject(method = "save", at = @At("TAIL"), remap = false, require = 0)
    private void polydium$fontrastSaved(CallbackInfo ci) {
        this.polydium$fontrastChanged();
    }

    @Unique
    private void polydium$fontrastChanged() {
        if (this.configClass().getName().equals(FontrastCompat.CONFIG_CLASS)) {
            FontrastCompat.configChanged(this.instance());
        }
    }
}
