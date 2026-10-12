package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.polyfrost.polydium.client.perf.MapColorTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = /*? if >= 1.21.4 {*/"net.minecraft.client.resources.MapTextureManager$MapInstance"/*?} else {*//*"net.minecraft.client.gui.MapRenderer$MapInstance"*//*?}*/)
public class MixinMapColorTable {
    @WrapOperation(
        method = /*? if >= 1.21.4 {*/"updateTextureIfNeeded"/*?} else {*//*"updateTexture"*//*?}*/,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/MapColor;getColorFromPackedId(I)I")
    )
    private int polydium$lookUpColor(int packedId, Operation<Integer> original) {
        return MapColorTable.get(packedId);
    }
}
//?}
