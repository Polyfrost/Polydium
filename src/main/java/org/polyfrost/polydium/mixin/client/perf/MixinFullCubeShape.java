package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.4 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.polyfrost.polydium.client.perf.FullCubeShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinFullCubeShape {
    @ModifyExpressionValue(
            method = "initCache",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/VoxelShape;getFaceShape(Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;")
    )
    private VoxelShape polydium$canonicalFullFace(VoxelShape face) {
        return FullCubeShape.canonical(face);
    }
}
//?}
