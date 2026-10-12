package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
//? if >= 26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(LoadingOverlay.class)
public abstract class MixinHideReloadOverlay {
    @Shadow
    @Final
    private boolean fadeIn;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private ReloadInstance reload;

    @Shadow
    @Final
    private Consumer<Optional<Throwable>> onFinish;

    @Shadow
    private long fadeOutStart;

    @WrapMethod(method = /*? if >= 26.1 {*/"extractRenderState"/*?} else {*//*"render"*//*?}*/)
    private void polydium$skipReloadRender(/*? if >= 26.1 {*/GuiGraphicsExtractor/*?} else {*//*GuiGraphics*//*?}*/ graphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        if (!PolydiumToggles.hideReloadOverlay) {
            original.call(graphics, mouseX, mouseY, partialTick);
            return;
        }
        if (!this.fadeIn) {
            if (this.fadeOutStart != -1L) {
                this.polydium$closeOverlay();
            } else {
                original.call(graphics, mouseX, mouseY, partialTick);
            }
            return;
        }
        if (this.minecraft.level == null && this.polydium$screen() == null) {
            original.call(graphics, mouseX, mouseY, partialTick);
            return;
        }
        if (this.minecraft.level == null) {
            this.polydium$screen()./*? if >= 26.1 {*/extractRenderStateWithTooltipAndSubtitles/*?} else if >= 1.21.10 {*//*renderWithTooltipAndSubtitles*//*?} else {*//*renderWithTooltip*//*?}*/(graphics, mouseX, mouseY, partialTick);
        }
        if (this.fadeOutStart == -1L && this.reload.isDone()) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable t) {
                this.onFinish.accept(Optional.of(t));
            }
            if (this.polydium$screen() != null) {
                Window window = this.minecraft.getWindow();
                this.polydium$screen().init(/*? if < 1.21.11 {*//*this.minecraft, *//*?}*/window.getGuiScaledWidth(), window.getGuiScaledHeight());
            }
            this.polydium$closeOverlay();
        }
    }

    @Unique
    private Screen polydium$screen() {
        //? if >= 26.2 {
        return this.minecraft.gui.screen();
        //?} else {
        /*return this.minecraft.screen;
        *///?}
    }

    @Unique
    private void polydium$closeOverlay() {
        //? if >= 26.2 {
        this.minecraft.gui.setOverlay(null);
        //?} else {
        /*this.minecraft.setOverlay(null);
        *///?}
    }
}
//?}
