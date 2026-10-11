package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.polyfrost.polydium.client.perf.GuiItemBake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = /*? if >= 26.1 {*/"net.minecraft.client.renderer.item.CuboidItemModelWrapper"/*?} else {*//*"net.minecraft.client.renderer.item.BlockModelWrapper"*//*?}*/)
public class MixinGuiItemBakeModel {
    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;setAnimated()V", ordinal = 0))
    private void polydium$foil(ItemStackRenderState output, Operation<Void> original) {
        if (GuiItemBake.GLINT_INTERVAL > 0) {
            ((GuiItemBake.Access) output).polydium$bake(GuiItemBake.FOIL);
        } else {
            original.call(output);
        }
    }

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;setAnimated()V", ordinal = 1))
    private void polydium$animatedSprite(ItemStackRenderState output, Operation<Void> original) {
        ((GuiItemBake.Access) output).polydium$bake(GuiItemBake.SPRITE);
    }
}
//?}
