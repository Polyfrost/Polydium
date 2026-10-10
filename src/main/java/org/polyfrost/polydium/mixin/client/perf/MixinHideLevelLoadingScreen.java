package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import net.minecraft.client.Minecraft;
//? if >= 26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class MixinHideLevelLoadingScreen {
    //? if >= 26.1 {
    @Inject(method = {"extractRenderState", "extractBackground"}, at = @At("HEAD"), cancellable = true)
    private void polydium$skipRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = {"render", "renderBackground"}, at = @At("HEAD"), cancellable = true)
    private void polydium$skipRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
    *///?}
        if (Minecraft.getInstance().level != null) {
            ci.cancel();
        }
    }
}
//?}
