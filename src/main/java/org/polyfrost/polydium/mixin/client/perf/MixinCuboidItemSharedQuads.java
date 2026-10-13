package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.5 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.renderer.item./^? if >= 26.1 {^/CuboidItemModelWrapper/^?} else {^//^BlockModelWrapper^//^?}^/;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client./^? if >= 26.1 {^/resources.model.geometry/^?} else {^//^renderer.block.model^//^?}^/.BakedQuad;
import org.polyfrost.polydium.client.perf.SharedItemQuads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(/^? if >= 26.1 {^/CuboidItemModelWrapper/^?} else {^//^BlockModelWrapper^//^?}^/.class)
public abstract class MixinCuboidItemSharedQuads {
    @SuppressWarnings("unchecked")
    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private boolean polydium$shareQuads(List<BakedQuad> target, Collection<BakedQuad> source, Operation<Boolean> original,
                                        @Local ItemStackRenderState.LayerRenderState layer) {
        if (!target.isEmpty() || !(source instanceof List) || target != layer.prepareQuadList()) {
            return original.call(target, source);
        }
        ((SharedItemQuads) (Object) layer).polydium$shareQuads((List<BakedQuad>) source);
        return !source.isEmpty();
    }
}
*///?}
