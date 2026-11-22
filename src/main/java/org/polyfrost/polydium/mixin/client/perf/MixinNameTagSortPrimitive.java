package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 && < 26.2 {
/*import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Comparator;

@Mixin(NameTagFeatureRenderer.class)
public class MixinNameTagSortPrimitive {
    @Unique
    private static final Comparator<SubmitNodeStorage.NameTagSubmit> polydium$FARTHEST_FIRST = (a, b) -> Double.compare(b.distanceToCameraSq(), a.distanceToCameraSq());

    @ModifyArg(method = /^? if >= 26.1 {^/"renderTranslucent"/^?} else {^//^"render"^//^?}^/, at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"))
    private Comparator<SubmitNodeStorage.NameTagSubmit> polydium$sortWithoutBoxing(Comparator<SubmitNodeStorage.NameTagSubmit> comparator) {
        return polydium$FARTHEST_FIRST;
    }
}
*///?}
