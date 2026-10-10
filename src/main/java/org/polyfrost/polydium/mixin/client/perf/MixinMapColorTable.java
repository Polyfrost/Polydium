package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.polyfrost.polydium.client.perf.MapColorTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.resources.MapTextureManager$MapInstance")
public class MixinMapColorTable {
    @WrapOperation(
        method = "updateTextureIfNeeded",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/MapColor;getColorFromPackedId(I)I")
    )
    private int polydium$lookUpColor(int packedId, Operation<Integer> original) {
        return MapColorTable.get(packedId);
    }
}
//?}
