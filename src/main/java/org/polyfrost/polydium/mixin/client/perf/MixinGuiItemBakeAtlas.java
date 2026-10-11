package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.render.DynamicAtlasAllocator;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import org.polyfrost.polydium.client.perf.GuiItemBake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiItemAtlas.class)
public class MixinGuiItemBakeAtlas {
    @ModifyExpressionValue(method = "getOrUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/DynamicAtlasAllocator$Slot;state()Lnet/minecraft/client/gui/render/DynamicAtlasAllocator$SlotState;"))
    private DynamicAtlasAllocator.SlotState polydium$refreshBaked(DynamicAtlasAllocator.SlotState state, @Local(argsOnly = true) TrackingItemStackRenderState item) {
        return GuiItemBake.refresh(item) && state == DynamicAtlasAllocator.SlotState.READY ? DynamicAtlasAllocator.SlotState.STALE : state;
    }
}
//?}
