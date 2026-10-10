package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.immediate.model.EntityRenderer", remap = false)
public abstract class MixinCuboidNormals {
    @WrapOperation(
            method = "prepareNormalsIfChanged",
            at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/math/MatrixHelper;transformNormal(Lorg/joml/Matrix3f;ZLnet/minecraft/core/Direction;)I"),
            require = 0,
            expect = 0
    )
    private static int polydium$flipOppositeNormal(Matrix3f matrix, boolean skipNormalization, Direction direction, Operation<Integer> original,
                                                   @Share("polydium$lastDirection") LocalRef<Direction> lastDirection,
                                                   @Share("polydium$lastNormal") LocalIntRef lastNormal) {
        if (lastDirection.get() == direction.getOpposite()) {
            return NormI8.flipPacked(lastNormal.get());
        }
        int normal = original.call(matrix, skipNormalization, direction);
        lastDirection.set(direction);
        lastNormal.set(normal);
        return normal;
    }
}
//?}
