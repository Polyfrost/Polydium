package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.feature.phase.TranslucentFeatureRenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TranslucentFeatureRenderPhase.class)
public abstract class MixinTranslucentFeatureStableSort {

    @ModifyReturnValue(method = "lambda$sortIndices$0", at = @At("RETURN"))
    private int polydium$tieBreakOnIndex(int byDistance, int i1, int i2) {
        return byDistance != 0 ? byDistance : Integer.compare(i1, i2);
    }
}
*///?}
