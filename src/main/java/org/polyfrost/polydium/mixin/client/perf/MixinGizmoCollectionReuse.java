package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class MixinGizmoCollectionReuse {
    @Unique
    private DrawableGizmoPrimitives polydium$standard;
    @Unique
    private DrawableGizmoPrimitives polydium$alwaysOnTop;

    @WrapOperation(
        method = "finalizeGizmoCollection",
        at = @At(value = "NEW", target = "()Lnet/minecraft/client/renderer/gizmos/DrawableGizmoPrimitives;", ordinal = 0)
    )
    private DrawableGizmoPrimitives polydium$reuseStandard(Operation<DrawableGizmoPrimitives> original) {
        DrawableGizmoPrimitives kept = this.polydium$standard;
        if (kept == null || !polydium$isEmpty(kept)) {
            this.polydium$standard = kept = original.call();
        }
        return kept;
    }

    @WrapOperation(
        method = "finalizeGizmoCollection",
        at = @At(value = "NEW", target = "()Lnet/minecraft/client/renderer/gizmos/DrawableGizmoPrimitives;", ordinal = 1)
    )
    private DrawableGizmoPrimitives polydium$reuseAlwaysOnTop(Operation<DrawableGizmoPrimitives> original) {
        DrawableGizmoPrimitives kept = this.polydium$alwaysOnTop;
        if (kept == null || !polydium$isEmpty(kept)) {
            this.polydium$alwaysOnTop = kept = original.call();
        }
        return kept;
    }

    @Unique
    private static boolean polydium$isEmpty(DrawableGizmoPrimitives primitives) {
        //? if >= 26.3 || < 26.2 {
        return primitives.isEmpty();
        //?} else {
        /*return ((DrawableGizmoPrimitivesAccessor) primitives).polydium$isEmpty();
        *///?}
    }
}
//?}
