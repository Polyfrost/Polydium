package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = /*? if >= 26.1 {*/"extractRenderState"/*?} else {*//*"render"*//*?}*/, at = @At("HEAD"), cancellable = true)
    private void polydium$skipReloadRender(/*? if >= 26.1 {*/GuiGraphicsExtractor/*?} else {*//*GuiGraphics*//*?}*/ graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!this.fadeIn) {
            if (this.fadeOutStart != -1L) {
                this.polydium$closeOverlay();
                ci.cancel();
            }
            return;
        }
        if (this.minecraft.level == null && this.polydium$screen() == null) {
            return;
        }
        if (this.minecraft.level == null) {
            this.polydium$screen()./*? if >= 26.1 {*/extractRenderStateWithTooltipAndSubtitles/*?} else {*//*renderWithTooltipAndSubtitles*//*?}*/(graphics, mouseX, mouseY, partialTick);
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
                this.polydium$screen().init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
            }
            this.polydium$closeOverlay();
        }
        ci.cancel();
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
