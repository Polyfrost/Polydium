package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client./*? if >= 26.1 {*/resources.model.cuboid/*?} else {*//*renderer.block.model*//*?}*/.ItemModelGenerator;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.polyfrost.polydium.client.perf.UpscaledSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemModelGenerator.class)
public abstract class MixinItemModelUpscale {
    @WrapOperation(method = {"getSideFaces", /*? if >= 26.1 {*/"bakeSideFaces"/*?} else {*//*"createSideElements"*//*?}*/}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/SpriteContents;width()I"))
    private static int polydium$unscaledWidth(SpriteContents sprite, Operation<Integer> original) {
        return original.call(sprite) / ((UpscaledSprite) sprite).polydium$upscale();
    }

    @WrapOperation(method = {"getSideFaces", /*? if >= 26.1 {*/"bakeSideFaces"/*?} else {*//*"createSideElements"*//*?}*/}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/SpriteContents;height()I"))
    private static int polydium$unscaledHeight(SpriteContents sprite, Operation<Integer> original) {
        return original.call(sprite) / ((UpscaledSprite) sprite).polydium$upscale();
    }

    @WrapOperation(method = "isTransparent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/SpriteContents;isTransparent(III)Z"))
    private static boolean polydium$unscaledTransparent(SpriteContents sprite, int frame, int x, int y, Operation<Boolean> original, @Local(argsOnly = true, ordinal = 3) int width) {
        if (width == sprite.width())
            return original.call(sprite, frame, x, y);
        int factor = ((UpscaledSprite) sprite).polydium$upscale();
        return original.call(sprite, frame, x * factor, y * factor);
    }
}
//?}
