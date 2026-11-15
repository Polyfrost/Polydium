package org.polyfrost.polydium.mixin.client.perf.lambdabettergrass;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 26.1 {
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.BlockGetter;
//?} else if >= 1.21.5 {
/*import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.world.level.BlockAndTintGetter;
*///?} else {
/*import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.BlockAndTintGetter;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "dev.lambdaurora.lambdabettergrass.metadata.layer.LBGCompiledLayerMetadata", remap = false)
public abstract class MixinLayerAlternateScanSkip {
    @Shadow
    //? if >= 1.21.5 {
    private BlockStateModel bakedAlternateModel;
    //?} else
    /*private BakedModel bakedAlternateModel;*/

    @WrapOperation(method = "emitBlockQuads", at = @At(value = "INVOKE", target = "Ldev/lambdaurora/lambdabettergrass/metadata/layer/LBGLayerType;getNearbyLayeredBlocks", ordinal = 1), require = 0)
    //? if >= 26.1 {
    private int polydium$skipUnusedScan(@Coerce Object type, BlockGetter level, BlockPos pos, Block block, boolean onlySource, Operation<Integer> original) {
    //?} else
    /*private int polydium$skipUnusedScan(@Coerce Object type, BlockAndTintGetter level, BlockPos pos, Block block, boolean onlySource, Operation<Integer> original) {*/
        return this.bakedAlternateModel == null ? 0 : original.call(type, level, pos, block, onlySource);
    }
}
