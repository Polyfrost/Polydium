package org.polyfrost.polydium.mixin.client.perf;

//? if < 1.21.5 {
/*import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.SpriteTransparency;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 1.21.4 {
import net.minecraft.client.renderer.item.BlockModelWrapper;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
//?} else {
/^import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
^///?}

//? if >= 1.21.4 {
@Mixin(BlockModelWrapper.class)
public abstract class MixinOpaqueItemCutoutModel {
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;setupBlockModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/client/renderer/RenderType;)V"))
    private void polydium$cutoutOpaqueItem(Args args) {
        RenderType type = args.get(1);
        if (PolydiumToggles.opaqueItemCutout && type == Sheets.translucentItemSheet() && !SpriteTransparency.modelHasTranslucent(args.<BakedModel>get(0))) {
            args.set(1, Sheets.cutoutBlockSheet());
        }
    }
}
//?} else {
/^@Mixin(ItemRenderer.class)
public abstract class MixinOpaqueItemCutoutModel {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getRenderType(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType polydium$cutoutOpaqueItem(ItemStack stack, boolean fabulous, Operation<RenderType> original, @Local(argsOnly = true) BakedModel model) {
        RenderType type = original.call(stack, fabulous);
        if (PolydiumToggles.opaqueItemCutout && (type == Sheets.translucentItemSheet() || type == Sheets.translucentCullBlockSheet()) && !SpriteTransparency.modelHasTranslucent(model)) {
            return Sheets.cutoutBlockSheet();
        }
        return type;
    }
}
^///?}
*///?}
