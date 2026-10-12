package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SheetedDecalTextureGenerator.class)
public abstract class MixinDecalRotationCache {
    @Unique
    private static final Quaternionf[] polydium$ROTATIONS = polydium$rotations();

    @WrapOperation(method = "setNormal", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;getRotation()Lorg/joml/Quaternionf;"))
    private Quaternionf polydium$cachedRotation(Direction direction, Operation<Quaternionf> original) {
        return polydium$ROTATIONS[direction.ordinal()];
    }

    //? if >= 26.3 {
    @WrapOperation(method = "setSheetedDecalUv", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;getRotation()Lorg/joml/Quaternionf;"))
    private static Quaternionf polydium$cachedStaticRotation(Direction direction, Operation<Quaternionf> original) {
        return polydium$ROTATIONS[direction.ordinal()];
    }
    //?}

    @Unique
    private static Quaternionf[] polydium$rotations() {
        Direction[] directions = Direction.values();
        Quaternionf[] rotations = new Quaternionf[directions.length];
        for (Direction direction : directions) {
            rotations[direction.ordinal()] = direction.getRotation();
        }
        return rotations;
    }
}
//?}
