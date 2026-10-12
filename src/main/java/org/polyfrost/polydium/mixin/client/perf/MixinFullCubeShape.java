package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//? if >= 1.21.4
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.polyfrost.polydium.client.perf.FullCubeShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >= 1.21.4 {
@Mixin(BlockBehaviour.BlockStateBase.class)
//?} else {
/*@Mixin(targets = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase$Cache")
*///?}
public abstract class MixinFullCubeShape {
    @ModifyExpressionValue(
            //? if >= 1.21.4 {
            method = "initCache",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/VoxelShape;getFaceShape(Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;")
            //?} else {
            /*method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/Shapes;getFaceShape(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;")
            *///?}
    )
    private VoxelShape polydium$canonicalFullFace(VoxelShape face) {
        return FullCubeShape.canonical(face);
    }
}
//?}
