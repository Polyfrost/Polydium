package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
//? if >= 26.1 {
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
//?} else {
/*import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
*///?}
import org.polyfrost.polydium.client.perf.DebugChartMerge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public class MixinDebugChartMergeState {
    @Inject(method = /*? if >= 26.1 {*/"addGuiElement"/*?} else {*//*"submitGuiElement"*//*?}*/, at = @At("HEAD"), cancellable = true)
    private void polydium$holdRectangle(GuiElementRenderState element, CallbackInfo ci) {
        if (DebugChartMerge.hold((GuiRenderState) (Object) this, element)) {
            ci.cancel();
        }
    }

    //? if >= 26.1 {
    @Inject(method = {"addText", "addItem", "addPicturesInPictureState", "addBlitToCurrentLayer", "addGlyphToCurrentLayer", "nextStratum", "blurBeforeThisStratum", "up", "reset"}, at = @At("HEAD"))
    //?} else if >= 1.21.10 {
    /*@Inject(method = {"submitText", "submitItem", "submitPicturesInPictureState", "submitBlitToCurrentLayer", "submitGlyphToCurrentLayer", "nextStratum", "blurBeforeThisStratum", "up", "reset"}, at = @At("HEAD"))
    *///?} else {
    /*@Inject(method = {"submitText", "submitItem", "submitPicturesInPictureState", "submitBlitToCurrentLayer", "submitGlyphToCurrentLayer", "nextStratum", "blurBeforeThisStratum", "up", "down", "reset"}, at = @At("HEAD"))
    *///?}
    private void polydium$flushRectangles(CallbackInfo ci) {
        DebugChartMerge.flush();
    }
}
//?}
