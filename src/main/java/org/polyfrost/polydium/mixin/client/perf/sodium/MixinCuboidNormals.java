package org.polyfrost.polydium.mixin.client.perf.sodium;

import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.immediate.model.EntityRenderer", remap = false)
public abstract class MixinCuboidNormals {
    @Unique
    private static Direction polydium$lastDirection;
    @Unique
    private static int polydium$lastNormal;

    @Redirect(
            method = "prepareNormalsIfChanged",
            at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/math/MatrixHelper;transformNormal(Lorg/joml/Matrix3f;ZLnet/minecraft/core/Direction;)I"),
            require = 0,
            expect = 0
    )
    private static int polydium$flipOppositeNormal(Matrix3f matrix, boolean skipNormalization, Direction direction) {
        if (polydium$lastDirection == direction.getOpposite()) {
            polydium$lastDirection = null;
            return NormI8.flipPacked(polydium$lastNormal);
        }
        int normal = MatrixHelper.transformNormal(matrix, skipNormalization, direction);
        polydium$lastDirection = direction;
        polydium$lastNormal = normal;
        return normal;
    }
}
