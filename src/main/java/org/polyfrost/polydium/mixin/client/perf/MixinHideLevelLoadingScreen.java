package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
//? if >= 26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if >= 1.21.10 {
import net.minecraft.client.gui.screens.LevelLoadingScreen;
//?} else {
/*import net.minecraft.client.gui.screens.ReceivingLevelScreen;
*///?}
import org.spongepowered.asm.mixin.Mixin;

@Mixin(/*? if >= 1.21.10 {*/LevelLoadingScreen/*?} else {*//*ReceivingLevelScreen*//*?}*/.class)
public abstract class MixinHideLevelLoadingScreen {
    //? if >= 26.1 {
    @WrapMethod(method = {"extractRenderState", "extractBackground"})
    private void polydium$skipRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
    //?} else {
    /*@WrapMethod(method = {"render", "renderBackground"})
    private void polydium$skipRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
    *///?}
        if (!PolydiumToggles.hideLevelLoadingScreen || Minecraft.getInstance().level == null) {
            original.call(graphics, mouseX, mouseY, partialTick);
        }
    }
}
//?}
