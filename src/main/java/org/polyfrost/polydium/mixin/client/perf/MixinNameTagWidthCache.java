package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
//? if >= 26.2 {
import net.minecraft.client.renderer.SubmitNodeCollection;
//?} else if >= 1.21.10 {
/*import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
*///?} else {
/*import net.minecraft.client.renderer.entity.EntityRenderer;
*///?}
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import org.polyfrost.polydium.client.perf.NameTagReuse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >= 26.2 {
@Mixin(value = SubmitNodeCollection.class, priority = 900)
//?} else if >= 1.21.10 {
/*@Mixin(value = NameTagFeatureRenderer.Storage.class, priority = 900)
*///?} else {
/*@Mixin(value = EntityRenderer.class, priority = 900)
*///?}
public abstract class MixinNameTagWidthCache {
    //? if >= 26.2 {
    @WrapOperation(method = "submitNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I"))
    //?} else if >= 1.21.10 {
    /*@WrapOperation(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I"))
    *///?} else {
    /*@WrapOperation(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I"))
    *///?}
    private int polydium$cachedNameWidth(Font font, FormattedText text, Operation<Integer> original) {
        if (!(text instanceof MutableComponent component) || !RenderSystem.isOnRenderThread()) {
            return original.call(font, text);
        }
        int width = NameTagReuse.width(font, component);
        if (width == NameTagReuse.NO_WIDTH) {
            width = original.call(font, text);
            NameTagReuse.putWidth(font, component, width);
        }
        return width;
    }
}
