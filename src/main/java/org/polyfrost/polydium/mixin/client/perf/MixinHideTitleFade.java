package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if >= 1.21.1 {
//? if >= 26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinHideTitleFade {
    @Shadow
    private boolean fading;

    //? if >= 26.1 {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void polydium$skipFade(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void polydium$skipFade(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
    *///?}
        if (!PolydiumToggles.hideTitleFade)
            return;
        this.fading = false;
    }
}
//?}
